package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.ArticleQaRequest;
import com.medicology.dictionary.dto.response.ArticleQaResponse;
import com.medicology.dictionary.dto.response.ArticleQaSuggestedQuestionsResponse;
import com.medicology.dictionary.service.ArticleQaService;
import com.medicology.dictionary.service.AuthenticatedUserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionary/articles/{articleId}/qa")
@RequiredArgsConstructor
public class ArticleQaController {

    private final ArticleQaService articleQaService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping
    public ResponseEntity<ArticleQaResponse> ask(
            @PathVariable UUID articleId, @RequestBody ArticleQaRequest request) {
        UUID userId = authenticatedUserService.getCurrentUserId();
        return ResponseEntity.ok(articleQaService.ask(userId, articleId, request));
    }

    @GetMapping("/suggested-questions")
    public ResponseEntity<ArticleQaSuggestedQuestionsResponse> suggestedQuestions(@PathVariable UUID articleId) {
        authenticatedUserService.getCurrentUserId();
        return ResponseEntity.ok(articleQaService.suggestedQuestions(articleId));
    }
}
