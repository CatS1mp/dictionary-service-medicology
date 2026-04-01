package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.ArticleRequest;
import com.medicology.dictionary.dto.response.ArticleResponse;
import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    @Transactional
    public UUID createArticle(ArticleRequest request) {
        Article article = Article.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .contentMarkdown(request.getContentMarkdown())
                .themeId(request.getThemeId())
                .authorAdminId(request.getAuthorAdminId())
                .isPublished(false)
                .build();
        return articleRepository.save(article).getId();
    }

    public ArticleResponse getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlugAndIsPublishedTrue(slug)
            .orElseThrow(() -> new RuntimeException("Article not found"));
        return mapToResponse(article);
    }
    
    public List<ArticleResponse> getAllArticles() {
        return articleRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void publishArticle(UUID articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new RuntimeException("Article not found"));
        article.setIsPublished(true);
        article.setPublishedAt(LocalDateTime.now());
        articleRepository.save(article);
    }

    @Transactional
    public void updateArticle(UUID id, ArticleRequest request) {
        Article article = articleRepository.findById(id).orElseThrow();
        article.setName(request.getName());
        article.setSlug(request.getSlug());
        article.setContentMarkdown(request.getContentMarkdown());
        article.setThemeId(request.getThemeId());
        articleRepository.save(article);
    }

    @Transactional
    public void deleteArticle(UUID id) {
        articleRepository.deleteById(id);
    }

    private ArticleResponse mapToResponse(Article article) {
        ArticleResponse res = new ArticleResponse();
        res.setId(article.getId());
        res.setThemeId(article.getThemeId());
        res.setName(article.getName());
        res.setSlug(article.getSlug());
        res.setContentMarkdown(article.getContentMarkdown());
        res.setAuthorAdminId(article.getAuthorAdminId());
        res.setIsPublished(article.getIsPublished());
        res.setPublishedAt(article.getPublishedAt());
        res.setCreatedAt(article.getCreatedAt());
        res.setUpdatedAt(article.getUpdatedAt());
        return res;
    }
}
