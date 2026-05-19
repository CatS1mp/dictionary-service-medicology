package com.medicology.dictionary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.dictionary.config.DictionaryAiProperties;
import com.medicology.dictionary.dto.request.ArticleQaConversationMessage;
import com.medicology.dictionary.dto.request.ArticleQaRequest;
import com.medicology.dictionary.dto.response.ArticleQaCitationResponse;
import com.medicology.dictionary.dto.response.ArticleQaResponse;
import com.medicology.dictionary.dto.response.ArticleQaSuggestedQuestionsResponse;
import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.service.ai.GeminiGenerateContentClient;
import com.medicology.dictionary.service.ai.GeminiGenerateOptions;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ArticleQaService {

    private static final Logger log = LoggerFactory.getLogger(ArticleQaService.class);
    private static final String DISCLAIMER =
            "Thông tin tham khảo từ bài viết, không thay thế tư vấn y khoa chuyên môn.";

    private final DictionaryAiProperties aiProperties;
    private final ArticleRepository articleRepository;
    private final ArticleContentContextBuilder contentContextBuilder;
    private final GeminiGenerateContentClient geminiClient;
    private final ArticleQaRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public ArticleQaResponse ask(UUID userId, UUID articleId, ArticleQaRequest request) {
        String question = normalizeQuestion(request == null ? null : request.getQuestion());

        Article article = articleRepository
                .findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết."));
        if (!Boolean.TRUE.equals(article.getIsPublished())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bài viết chưa được xuất bản.");
        }

        ensureQaEnabled();
        if (!rateLimiter.tryConsume(userId, aiProperties.getQaDailyLimitPerUser())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Đã vượt giới hạn câu hỏi AI trong ngày.");
        }
        List<ArticleContentSection> sections = contentContextBuilder.buildSections(article);
        String articleContext = contentContextBuilder.buildPromptContext(
                sections, question, Math.max(1000, aiProperties.getQaMaxContextChars()));

        if (articleContext.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài viết không có nội dung để hỏi đáp.");
        }

        String prompt = buildPrompt(article, articleContext, question, normalizeConversation(request));
        String aiJson = geminiClient
                .generateJsonText(prompt, GeminiGenerateOptions.jsonDefaults())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ AI tạm thời không khả dụng."));

        try {
            JsonNode parsed = objectMapper.readTree(aiJson);
            boolean outOfScope = parsed.path("outOfScope").asBoolean(false);
            String answer = parsed.path("answer").asText("").trim();
            String confidence = normalizeConfidence(parsed.path("confidence").asText("medium"));
            Set<String> validSectionIds = sections.stream().map(ArticleContentSection::id).collect(Collectors.toSet());
            List<ArticleQaCitationResponse> citations = parseCitations(parsed.path("citations"), validSectionIds);

            if (!outOfScope && answer.isBlank()) {
                answer = "Không tìm thấy thông tin phù hợp trong bài viết.";
                outOfScope = true;
            }

            log.info(
                    "dictionary_article_qa_completed userId={} articleId={} questionLength={} outOfScope={}",
                    userId,
                    articleId,
                    question.length(),
                    outOfScope);

            return ArticleQaResponse.builder()
                    .answer(answer)
                    .citations(outOfScope ? List.of() : citations)
                    .outOfScope(outOfScope)
                    .confidence(confidence)
                    .disclaimer(DISCLAIMER)
                    .build();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("dictionary_article_qa_parse_failed articleId={} message={}", articleId, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không phân tích được phản hồi AI.");
        }
    }

    public ArticleQaSuggestedQuestionsResponse suggestedQuestions(UUID articleId) {
        Article article = articleRepository
                .findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết."));
        if (!Boolean.TRUE.equals(article.getIsPublished())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bài viết chưa được xuất bản.");
        }

        List<ArticleContentSection> sections = contentContextBuilder.buildSections(article);
        LinkedHashSet<String> questions = new LinkedHashSet<>();
        questions.add("Tóm tắt nội dung chính của bài viết");

        for (ArticleContentSection section : sections) {
            if (questions.size() >= 4) {
                break;
            }
            if (section.heading() == null || section.heading().isBlank()) {
                continue;
            }
            questions.add("Giải thích mục: " + section.heading());
        }

        return ArticleQaSuggestedQuestionsResponse.builder()
                .questions(questions.stream().limit(4).toList())
                .build();
    }

    private void ensureQaEnabled() {
        if (!aiProperties.isQaEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Hỏi đáp bài viết đang tắt.");
        }
        if (!geminiClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ AI chưa được cấu hình.");
        }
    }

    private String buildPrompt(
            Article article,
            String articleContext,
            String question,
            List<ArticleQaConversationMessage> conversation) {
        String history = conversation.stream()
                .map(message -> "- %s: %s".formatted(safe(message.getRole()), safe(message.getContent())))
                .collect(Collectors.joining("\n"));

        return """
                You are a medical reading assistant. Answer ONLY using the article sections below.
                Article title: %s

                Article sections:
                %s

                Recent conversation:
                %s

                User question:
                %s

                Return ONLY valid JSON:
                {
                  "answer": "string",
                  "outOfScope": boolean,
                  "confidence": "high|medium|low",
                  "citations": [
                    {
                      "sectionId": "string",
                      "heading": "string",
                      "quote": "short quote from section"
                    }
                  ]
                }

                Rules:
                - If the question is unrelated to the article, set outOfScope=true and answer briefly that the article does not cover it.
                - Do not provide personal diagnosis or treatment plans.
                - Do not invent drugs, doses, or facts outside the article.
                - When outOfScope=false, include at least one citation with a valid sectionId from the article sections.
                - Keep answer concise and practical in Vietnamese with full diacritics (tiếng Việt có dấu).
                - When outOfScope=false, reason in citations is not required; answer must use proper Vietnamese diacritics.
                - quote must be short (max 200 chars).
                """.formatted(
                safe(article.getName()),
                articleContext,
                history.isBlank() ? "(none)" : history,
                question);
    }

    private List<ArticleQaCitationResponse> parseCitations(JsonNode citationsNode, Set<String> validSectionIds) {
        if (citationsNode == null || !citationsNode.isArray()) {
            return List.of();
        }
        List<ArticleQaCitationResponse> citations = new ArrayList<>();
        for (JsonNode node : citationsNode) {
            String sectionId = node.path("sectionId").asText("").trim();
            if (!validSectionIds.contains(sectionId)) {
                continue;
            }
            String heading = node.path("heading").asText("").trim();
            String quote = truncate(node.path("quote").asText("").trim(), 200);
            citations.add(ArticleQaCitationResponse.builder()
                    .sectionId(sectionId)
                    .heading(heading)
                    .quote(quote)
                    .build());
        }
        return citations;
    }

    private List<ArticleQaConversationMessage> normalizeConversation(ArticleQaRequest request) {
        if (request == null || request.getConversation() == null) {
            return List.of();
        }
        return request.getConversation().stream()
                .filter(message -> message != null && message.getContent() != null && !message.getContent().isBlank())
                .filter(message -> {
                    String role = message.getRole() == null ? "" : message.getRole().trim().toLowerCase(Locale.ROOT);
                    return "user".equals(role) || "assistant".equals(role);
                })
                .map(message -> {
                    ArticleQaConversationMessage normalized = new ArticleQaConversationMessage();
                    normalized.setRole(message.getRole().trim().toLowerCase(Locale.ROOT));
                    normalized.setContent(truncate(message.getContent().trim(), 800));
                    return normalized;
                })
                .limit(4)
                .toList();
    }

    private String normalizeQuestion(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Câu hỏi là bắt buộc.");
        }
        String trimmed = raw.trim();
        int max = Math.max(50, aiProperties.getQaMaxQuestionChars());
        if (trimmed.length() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Câu hỏi quá dài.");
        }
        return trimmed;
    }

    private String normalizeConfidence(String raw) {
        if (raw == null) {
            return "medium";
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if ("high".equals(value) || "low".equals(value) || "medium".equals(value)) {
            return value;
        }
        return "medium";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
