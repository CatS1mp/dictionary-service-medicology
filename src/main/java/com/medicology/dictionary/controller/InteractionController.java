package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.response.InteractionSummaryResponse;
import com.medicology.dictionary.dto.response.ViewStatisticsResponse;
import com.medicology.dictionary.service.AuthenticatedUserService;
import com.medicology.dictionary.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary/articles/{articleId}")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/view")
    public ResponseEntity<Void> recordView(@PathVariable UUID articleId) {
        UUID userId = authenticatedUserService.getCurrentUserId();
        interactionService.recordView(articleId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bookmark")
    public ResponseEntity<Void> bookmarkArticle(@PathVariable UUID articleId) {
        UUID userId = authenticatedUserService.getCurrentUserId();
        interactionService.addBookmark(articleId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bookmark")
    public ResponseEntity<Void> removeBookmark(@PathVariable UUID articleId) {
        UUID userId = authenticatedUserService.getCurrentUserId();
        interactionService.removeBookmark(articleId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/interactions/summary")
    public ResponseEntity<InteractionSummaryResponse> getInteractionSummary(@PathVariable UUID articleId) {
        return ResponseEntity.ok(interactionService.getInteractionSummary(articleId));
    }

    @GetMapping("/views")
    public ResponseEntity<ViewStatisticsResponse> getViewStatistics(@PathVariable UUID articleId) {
        return ResponseEntity.ok(interactionService.getViewStatistics(articleId));
    }

}
