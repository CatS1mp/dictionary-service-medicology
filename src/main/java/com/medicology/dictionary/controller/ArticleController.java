package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.ArticleRequest;
import com.medicology.dictionary.dto.request.RelatedArticleRequest;
import com.medicology.dictionary.dto.response.ArticleResponse;
import com.medicology.dictionary.dto.response.TagResponse;
import com.medicology.dictionary.service.ArticleService;
import com.medicology.dictionary.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final TagService tagService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UUID> createArticle(@RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.createArticle(request));
    }

    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAllArticles(Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return ResponseEntity.ok(articleService.getAllArticles(admin));
    }

    @GetMapping("/{id:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<ArticleResponse> getArticleById(@PathVariable UUID id, Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return ResponseEntity.ok(articleService.getArticleById(id, admin));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ArticleResponse> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getArticleBySlug(slug));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateArticle(@PathVariable UUID id, @RequestBody ArticleRequest request) {
        articleService.updateArticle(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> publishArticle(@PathVariable UUID id) {
        articleService.publishArticle(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unpublishArticle(@PathVariable UUID id) {
        articleService.unpublishArticle(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignTags(@PathVariable UUID id, @RequestBody List<UUID> tagIds) {
        tagService.assignTagsToArticle(id, tagIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/tags")
    public ResponseEntity<List<TagResponse>> getTagsByArticle(@PathVariable UUID id) {
        return ResponseEntity.ok(tagService.getTagsByArticle(id));
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeTagFromArticle(@PathVariable UUID id, @PathVariable UUID tagId) {
        tagService.removeTagFromArticle(id, tagId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/related")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addRelatedArticle(@PathVariable UUID id, @RequestBody RelatedArticleRequest request) {
        articleService.addRelatedArticle(id, request.getRelatedArticleId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<ArticleResponse>> getRelatedArticles(@PathVariable UUID id) {
        return ResponseEntity.ok(articleService.getRelatedArticles(id));
    }

    @DeleteMapping("/{id}/related/{relatedId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRelatedArticle(@PathVariable UUID id, @PathVariable UUID relatedId) {
        articleService.removeRelatedArticle(id, relatedId);
        return ResponseEntity.ok().build();
    }
}
