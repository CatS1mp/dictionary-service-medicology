package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.ArticleRequest;
import com.medicology.dictionary.dto.response.ArticleResponse;
import com.medicology.dictionary.service.ArticleService;
import com.medicology.dictionary.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UUID> createArticle(@RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.createArticle(request));
    }

    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ArticleResponse> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getArticleBySlug(slug));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateArticle(@PathVariable UUID id, @RequestBody ArticleRequest request) {
        articleService.updateArticle(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishArticle(@PathVariable UUID id) {
        articleService.publishArticle(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<Void> assignTags(@PathVariable UUID id, @RequestBody List<UUID> tagIds) {
        tagService.assignTagsToArticle(id, tagIds);
        return ResponseEntity.ok().build();
    }
}
