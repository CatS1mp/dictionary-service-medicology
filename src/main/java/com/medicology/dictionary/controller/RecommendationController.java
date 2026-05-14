package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.ArticleRecommendationRequest;
import com.medicology.dictionary.dto.response.ArticleRecommendationResponse;
import com.medicology.dictionary.service.ArticleRecommendationService;
import com.medicology.dictionary.service.AuthenticatedUserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionary/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final ArticleRecommendationService articleRecommendationService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/articles")
    public ResponseEntity<ArticleRecommendationResponse> recommendArticles(
            @RequestBody(required = false) ArticleRecommendationRequest request) {
        UUID userId = authenticatedUserService.getCurrentUserId();
        return ResponseEntity.ok(articleRecommendationService.recommend(userId, request));
    }
}
