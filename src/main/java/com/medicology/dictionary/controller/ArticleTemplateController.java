package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.ArticleTemplateRequest;
import com.medicology.dictionary.dto.response.ArticleTemplateResponse;
import com.medicology.dictionary.service.ArticleTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary/templates")
@RequiredArgsConstructor
public class ArticleTemplateController {
    private final ArticleTemplateService articleTemplateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticleTemplateResponse> createTemplate(@RequestBody ArticleTemplateRequest request) {
        return ResponseEntity.ok(articleTemplateService.createTemplate(request));
    }

    @GetMapping
    public ResponseEntity<List<ArticleTemplateResponse>> getAllTemplates(
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(articleTemplateService.getAllTemplates(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleTemplateResponse> getTemplateById(@PathVariable UUID id) {
        return ResponseEntity.ok(articleTemplateService.getTemplateById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTemplate(@PathVariable UUID id, @RequestBody ArticleTemplateRequest request) {
        articleTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        articleTemplateService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }
}
