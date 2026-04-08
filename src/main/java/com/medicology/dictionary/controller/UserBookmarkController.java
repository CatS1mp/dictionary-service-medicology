package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.response.ArticleResponse;
import com.medicology.dictionary.service.AuthenticatedUserService;
import com.medicology.dictionary.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary/users/me/bookmarks")
@RequiredArgsConstructor
public class UserBookmarkController {
    private final InteractionService interactionService;
    private final AuthenticatedUserService authenticatedUserService;

    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getMyBookmarks() {
        UUID userId = authenticatedUserService.getCurrentUserId();
        return ResponseEntity.ok(interactionService.getBookmarks(userId));
    }
}
