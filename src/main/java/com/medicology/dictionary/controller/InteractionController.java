package com.medicology.dictionary.controller;

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

    @PostMapping("/view")
    public ResponseEntity<Void> recordView(@PathVariable UUID articleId) {
        UUID userId = UUID.randomUUID();
        interactionService.recordView(articleId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bookmark")
    public ResponseEntity<Void> bookmarkArticle(@PathVariable UUID articleId) {
        UUID userId = UUID.randomUUID();
        interactionService.toggleBookmark(articleId, userId);
        return ResponseEntity.ok().build();
    }
}
