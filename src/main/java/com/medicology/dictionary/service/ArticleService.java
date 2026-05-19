package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.ArticleRequest;
import com.medicology.dictionary.dto.response.ArticleResponse;
import com.medicology.dictionary.dto.response.TagResponse;
import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.entity.ArticleRelated;
import com.medicology.dictionary.entity.ArticleRelatedId;
import com.medicology.dictionary.entity.ArticleTag;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.ArticleRelatedRepository;
import com.medicology.dictionary.repository.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private static final int CONTENT_SCHEMA_VERSION = ContentJsonContractValidator.REQUIRED_CONTENT_VERSION;

    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final ArticleRelatedRepository articleRelatedRepository;
    private final ContentJsonContractValidator contentJsonContractValidator;

    @Transactional
    public UUID createArticle(ArticleRequest request) {
        validateContentContract(request);
        Article article = Article.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .contentJson(request.getContentJson())
                .contentVersion(CONTENT_SCHEMA_VERSION)
                .contentMarkdown(request.getContentMarkdown())
                .authorAdminId(request.getAuthorAdminId())
                .isPublished(false)
                .build();
        return articleRepository.save(article).getId();
    }

    public ArticleResponse getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlugAndIsPublishedTrue(slug)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy bài viết."));
        return mapToResponse(article);
    }

    public ArticleResponse getArticleById(UUID articleId, boolean includeUnpublished) {
        Article article = getArticleEntity(articleId);
        if (!includeUnpublished && !Boolean.TRUE.equals(article.getIsPublished())) {
            throw new ResponseStatusException(NOT_FOUND, "Không tìm thấy bài viết.");
        }
        return mapToResponse(article);
    }

    public List<ArticleResponse> getAllArticles(boolean includeUnpublished) {
        List<Article> articles = includeUnpublished
                ? articleRepository.findAll()
                : articleRepository.findAllByIsPublishedTrueOrderByUpdatedAtDesc();
        return articles.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void publishArticle(UUID articleId) {
        Article article = getArticleEntity(articleId);
        article.setIsPublished(true);
        article.setPublishedAt(LocalDateTime.now());
        articleRepository.save(article);
    }

    @Transactional
    public void unpublishArticle(UUID articleId) {
        Article article = getArticleEntity(articleId);
        article.setIsPublished(false);
        article.setPublishedAt(null);
        articleRepository.save(article);
    }

    @Transactional
    public void updateArticle(UUID id, ArticleRequest request) {
        validateContentContract(request);
        Article article = getArticleEntity(id);
        article.setName(request.getName());
        article.setSlug(request.getSlug());
        article.setContentJson(request.getContentJson());
        article.setContentVersion(CONTENT_SCHEMA_VERSION);
        article.setContentMarkdown(request.getContentMarkdown());
        article.setAuthorAdminId(request.getAuthorAdminId());
        articleRepository.save(article);
    }

    @Transactional
    public void deleteArticle(UUID id) {
        articleTagRepository.deleteByArticleId(id);
        articleRelatedRepository.deleteByArticleId(id);
        articleRelatedRepository.deleteByRelatedArticleId(id);
        articleRepository.deleteById(id);
    }

    @Transactional
    public void addRelatedArticle(UUID articleId, UUID relatedArticleId) {
        if (articleId.equals(relatedArticleId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Bài viết không thể liên kết với chính nó.");
        }

        Article article = getArticleEntity(articleId);
        Article relatedArticle = getArticleEntity(relatedArticleId);
        ArticleRelated relation = ArticleRelated.builder()
                .articleId(article.getId())
                .relatedArticleId(relatedArticle.getId())
                .build();
        if (!articleRelatedRepository.existsById(new ArticleRelatedId(articleId, relatedArticleId))) {
            articleRelatedRepository.save(relation);
        }
    }

    public List<ArticleResponse> getRelatedArticles(UUID articleId) {
        getArticleEntity(articleId);
        return articleRelatedRepository.findByArticleId(articleId).stream()
                .map(ArticleRelated::getRelatedArticle)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ArticleResponse toResponse(Article article) {
        return mapToResponse(article);
    }

    @Transactional
    public void removeRelatedArticle(UUID articleId, UUID relatedArticleId) {
        articleRelatedRepository.deleteByArticleIdAndRelatedArticleId(articleId, relatedArticleId);
    }

    private ArticleResponse mapToResponse(Article article) {
        ArticleResponse res = new ArticleResponse();
        res.setId(article.getId());
        res.setName(article.getName());
        res.setSlug(article.getSlug());
        res.setContentJson(article.getContentJson());
        res.setContentVersion(article.getContentVersion());
        res.setContentMarkdown(article.getContentMarkdown());
        res.setAuthorAdminId(article.getAuthorAdminId());
        res.setIsPublished(article.getIsPublished());
        res.setPublishedAt(article.getPublishedAt());
        res.setCreatedAt(article.getCreatedAt());
        res.setUpdatedAt(article.getUpdatedAt());
        res.setTags(articleTagRepository.findByArticleId(article.getId()).stream()
                .map(ArticleTag::getTag)
                .map(this::mapTagToResponse)
                .collect(Collectors.toList()));
        return res;
    }

    private TagResponse mapTagToResponse(com.medicology.dictionary.entity.Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }

    private Article getArticleEntity(UUID articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy bài viết."));
    }

    private void validateContentContract(ArticleRequest request) {
        if (request.getContentVersion() == null || request.getContentVersion() != CONTENT_SCHEMA_VERSION) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "contentVersion must be " + CONTENT_SCHEMA_VERSION
            );
        }
        contentJsonContractValidator.validateOrThrow(request.getContentJson());
    }
}
