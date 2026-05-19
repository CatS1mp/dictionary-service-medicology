package com.medicology.dictionary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.dictionary.config.DictionaryAiProperties;
import com.medicology.dictionary.dto.request.ArticleRecommendationRequest;
import com.medicology.dictionary.dto.request.RecommendationAttemptPayload;
import com.medicology.dictionary.dto.response.ArticleRecommendationItemResponse;
import com.medicology.dictionary.dto.response.ArticleRecommendationResponse;
import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.entity.ArticleTag;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.ArticleTagRepository;
import com.medicology.dictionary.repository.TagRepository;
import com.medicology.dictionary.repository.UserArticleViewRepository;
import com.medicology.dictionary.service.ai.GeminiGenerateContentClient;
import com.medicology.dictionary.service.ai.GeminiGenerateOptions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(ArticleRecommendationService.class);

    private final DictionaryAiProperties aiProperties;
    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final TagRepository tagRepository;
    private final UserArticleViewRepository userArticleViewRepository;
    private final ObjectMapper objectMapper;
    private final GeminiGenerateContentClient geminiClient;

    public ArticleRecommendationResponse recommend(UUID userId, ArticleRecommendationRequest request) {
        int requestedLimit = normalizeLimit(request == null ? null : request.getLimit());
        List<RecommendationAttemptPayload> attempts = request == null || request.getRecentAttempts() == null
                ? List.of()
                : request.getRecentAttempts();
        Set<String> contextTerms = collectContextTerms(attempts);

        List<ArticleCandidate> candidates = loadCandidates(contextTerms, userId);
        List<ArticleRecommendationItemResponse> aiItems = recommendWithAi(candidates, attempts, requestedLimit);
        if (!aiItems.isEmpty()) {
            return ArticleRecommendationResponse.builder()
                    .strategy("ai")
                    .items(aiItems)
                    .build();
        }

        List<ArticleRecommendationItemResponse> fallbackItems = fallbackPopularUnread(userId, requestedLimit);
        return ArticleRecommendationResponse.builder()
                .strategy("fallback_popular_unread")
                .items(fallbackItems)
                .build();
    }

    private List<ArticleCandidate> loadCandidates(Set<String> contextTerms, UUID userId) {
        List<Article> published = articleRepository.findAllByIsPublishedTrueOrderByUpdatedAtDesc();
        if (published.isEmpty()) {
            return List.of();
        }

        Set<UUID> readIds = userArticleViewRepository.findAll().stream()
                .filter(row -> userId.equals(row.getUserId()))
                .map(row -> row.getArticleId())
                .collect(Collectors.toSet());

        List<Article> unreadPublished = published.stream()
                .filter(article -> !readIds.contains(article.getId()))
                .toList();

        if (unreadPublished.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<String>> tagNamesByArticleId = loadTagNamesByArticleIds(
                unreadPublished.stream().map(Article::getId).toList());
        Map<UUID, Long> viewCountByArticleId = loadViewCounts();

        List<ArticleCandidate> scored = new ArrayList<>();
        for (Article article : unreadPublished) {
            List<String> tags = tagNamesByArticleId.getOrDefault(article.getId(), List.of());
            double ruleScore = computeRuleScore(article, tags, contextTerms);
            if (!contextTerms.isEmpty() && ruleScore <= 0) {
                continue;
            }
            scored.add(new ArticleCandidate(article, tags, viewCountByArticleId.getOrDefault(article.getId(), 0L), ruleScore));
        }

        scored.sort(Comparator
                .comparingDouble(ArticleCandidate::ruleScore).reversed()
                .thenComparingLong(ArticleCandidate::totalViews).reversed()
                .thenComparing(candidate -> candidate.article().getUpdatedAt(), Comparator.nullsLast(Comparator.reverseOrder())));

        int limit = Math.max(3, aiProperties.getCandidateLimit());
        if (scored.size() <= limit) {
            return scored;
        }
        return scored.subList(0, limit);
    }

    private List<ArticleRecommendationItemResponse> recommendWithAi(
            List<ArticleCandidate> candidates,
            List<RecommendationAttemptPayload> attempts,
            int limit) {
        if (candidates.isEmpty()) {
            return List.of();
        }
        if (!geminiClient.isConfigured()) {
            log.warn("dictionary_ai_recommendation_disabled reason=missing_api_key");
            return List.of();
        }

        try {
            String prompt = buildPrompt(attempts, candidates, limit);
            String aiJsonText = geminiClient
                    .generateJsonText(prompt, GeminiGenerateOptions.recommendation(aiProperties.isGroundingEnabled()))
                    .orElse("");
            if (aiJsonText.isBlank()) {
                return List.of();
            }

            JsonNode parsed = objectMapper.readTree(aiJsonText);
            JsonNode picks = parsed.path("recommendations");
            if (!picks.isArray()) {
                return List.of();
            }

            Map<UUID, ArticleCandidate> candidateById = candidates.stream()
                    .collect(Collectors.toMap(candidate -> candidate.article().getId(), Function.identity()));
            List<ArticleRecommendationItemResponse> output = new ArrayList<>();

            for (JsonNode pick : picks) {
                UUID articleId = parseUuid(pick.path("articleId").asText(""));
                if (articleId == null) {
                    continue;
                }
                ArticleCandidate candidate = candidateById.get(articleId);
                if (candidate == null) {
                    continue;
                }
                double score = normalizeScore(pick.path("score"));
                if (score < aiProperties.getRelevanceThreshold()) {
                    continue;
                }
                output.add(ArticleRecommendationItemResponse.builder()
                        .articleId(articleId)
                        .title(candidate.article().getName())
                        .slug(candidate.article().getSlug())
                        .matchScore(score)
                        .reason(normalizeReason(pick.path("reason").asText("")))
                        .source("ai")
                        .totalViews(candidate.totalViews())
                        .tags(candidate.tags())
                        .build());
                if (output.size() >= limit) {
                    break;
                }
            }

            return output;
        } catch (Exception ex) {
            log.warn("dictionary_ai_recommendation_failed message={}", ex.getMessage());
            return List.of();
        }
    }

    private List<ArticleRecommendationItemResponse> fallbackPopularUnread(UUID userId, int limit) {
        List<Article> fallbackArticles = articleRepository.findTopPublishedUnreadByViews(userId, Math.max(3, limit));
        if (fallbackArticles.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<String>> tagsByArticleId = loadTagNamesByArticleIds(
                fallbackArticles.stream().map(Article::getId).toList());
        Map<UUID, Long> viewsByArticleId = loadViewCounts();

        return fallbackArticles.stream()
                .limit(limit)
                .map(article -> ArticleRecommendationItemResponse.builder()
                        .articleId(article.getId())
                        .title(article.getName())
                        .slug(article.getSlug())
                        .matchScore(0d)
                        .reason("Không có bài đủ liên quan theo thẻ, ưu tiên bài được xem nhiều và bạn chưa đọc.")
                        .source("fallback_popular_unread")
                        .totalViews(viewsByArticleId.getOrDefault(article.getId(), 0L))
                        .tags(tagsByArticleId.getOrDefault(article.getId(), List.of()))
                        .build())
                .toList();
    }

    private String buildPrompt(List<RecommendationAttemptPayload> attempts, List<ArticleCandidate> candidates, int limit) {
        String attemptsContext = attempts.stream()
                .limit(8)
                .map(attempt -> {
                    String tags = attempt.getTags() == null ? "" : String.join(", ", attempt.getTags());
                    return "- contentId=%s, contentName=%s, tags=%s, submittedAt=%s, passed=%s"
                            .formatted(
                                    safeText(attempt.getContentId()),
                                    safeText(attempt.getContentName()),
                                    safeText(tags),
                                    safeText(attempt.getSubmittedAt()),
                                    attempt.getPassed() == null ? "unknown" : String.valueOf(attempt.getPassed()));
                })
                .collect(Collectors.joining("\n"));

        String articleContext = candidates.stream()
                .map(candidate -> "- articleId=%s, title=%s, tags=%s, totalViews=%d"
                        .formatted(
                                candidate.article().getId(),
                                safeText(candidate.article().getName()),
                                String.join(", ", candidate.tags()),
                                candidate.totalViews()))
                .collect(Collectors.joining("\n"));

        return """
                You are a recommendation engine for medical reading articles.
                Rank only the provided candidate articles by relevance to the user's latest learning attempts.
                Use semantic relation between attempt topics and article tags/titles.
                Return max %d items and keep confidence realistic.

                Learner recent attempts:
                %s

                Candidate articles:
                %s

                Return ONLY valid JSON:
                {
                  "recommendations": [
                    {
                      "articleId": "uuid",
                      "score": number,
                      "reason": "short reason"
                    }
                  ]
                }

                Rules:
                - score range must be [0, 1]
                - choose at most %d items
                - do not invent articleId outside candidate list
                - reason must be short, practical, in Vietnamese with full diacritics (tiếng Việt có dấu)
                """.formatted(limit, safeText(attemptsContext), safeText(articleContext), limit);
    }

    private Set<String> collectContextTerms(List<RecommendationAttemptPayload> attempts) {
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        for (RecommendationAttemptPayload attempt : attempts) {
            addTokens(terms, splitTerms(attempt.getContentName()));
            addTokens(terms, splitTerms(attempt.getContentId()));
            addTokens(terms, splitTermsCollection(attempt.getTags()));
        }
        return terms;
    }

    private Map<UUID, List<String>> loadTagNamesByArticleIds(List<UUID> articleIds) {
        if (articleIds.isEmpty()) {
            return Map.of();
        }
        List<ArticleTag> articleTags = articleTagRepository.findByArticleIdIn(articleIds);
        Map<UUID, String> tagNameById = tagRepository.findByIdIn(
                        articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(tag -> tag.getId(), tag -> tag.getName()));

        Map<UUID, List<String>> byArticle = new HashMap<>();
        for (ArticleTag row : articleTags) {
            byArticle.computeIfAbsent(row.getArticleId(), ignored -> new ArrayList<>())
                    .add(tagNameById.getOrDefault(row.getTagId(), ""));
        }
        byArticle.values().forEach(values -> values.removeIf(value -> value == null || value.isBlank()));
        return byArticle;
    }

    private Map<UUID, Long> loadViewCounts() {
        Map<UUID, Long> views = new HashMap<>();
        userArticleViewRepository.findAll().forEach(row ->
                views.merge(row.getArticleId(), Long.valueOf(row.getViewCount() == null ? 0 : row.getViewCount()), Long::sum));
        return views;
    }

    private double computeRuleScore(Article article, List<String> tags, Set<String> contextTerms) {
        if (contextTerms.isEmpty()) {
            return 0d;
        }
        Set<String> articleTerms = new LinkedHashSet<>();
        addTokens(articleTerms, splitTerms(article.getName()));
        addTokens(articleTerms, splitTerms(article.getSlug()));
        addTokens(articleTerms, splitTermsCollection(tags));
        if (articleTerms.isEmpty()) {
            return 0d;
        }
        long overlap = contextTerms.stream().filter(articleTerms::contains).count();
        return (double) overlap / (double) articleTerms.size();
    }

    private void addTokens(Set<String> target, Collection<String> values) {
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            target.add(value);
        }
    }

    private List<String> splitTermsCollection(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> all = new ArrayList<>();
        for (String value : values) {
            all.addAll(splitTerms(value));
        }
        return all;
    }

    private List<String> splitTerms(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        String[] parts = value.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (part.length() < 2) {
                continue;
            }
            tokens.add(part);
        }
        return tokens;
    }

    private int normalizeLimit(Integer requestLimit) {
        int fallback = 3;
        if (requestLimit == null) {
            return fallback;
        }
        if (requestLimit <= 0) {
            return fallback;
        }
        return Math.min(3, requestLimit);
    }

    private UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private double normalizeScore(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return 0d;
        }
        double score = node.isNumber() ? node.asDouble() : parseDoubleSafe(node.asText(""));
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            return 0d;
        }
        if (score < 0d) {
            return 0d;
        }
        if (score > 1d) {
            return 1d;
        }
        return score;
    }

    private double parseDoubleSafe(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return 0d;
        }
    }

    private String normalizeReason(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Bài viết liên quan đến các chủ đề bạn vừa học.";
        }
        return truncate(raw.trim(), 240);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, max) + "...";
    }

    private record ArticleCandidate(Article article, List<String> tags, long totalViews, double ruleScore) {}
}
