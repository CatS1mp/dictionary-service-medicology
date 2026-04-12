package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.CommentRequest;
import com.medicology.dictionary.dto.request.CommentStatusRequest;
import com.medicology.dictionary.dto.request.VoteRequest;
import com.medicology.dictionary.dto.response.CommentResponse;
import com.medicology.dictionary.service.AuthenticatedUserService;
import com.medicology.dictionary.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/articles/{articleId}/comments")
    public ResponseEntity<UUID> createComment(@PathVariable UUID articleId, @RequestBody CommentRequest request) {
        UUID userId = authenticatedUserService.getCurrentUserId();
        return ResponseEntity.ok(commentService.createComment(articleId, userId, request));
    }

    @GetMapping("/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID articleId) {
        return ResponseEntity.ok(commentService.getComments(articleId));
    }

    @PostMapping("/comments/{id}/reply")
    public ResponseEntity<UUID> replyComment(@PathVariable UUID id, @RequestBody CommentRequest request) {
        UUID userId = authenticatedUserService.getCurrentUserId();
        return ResponseEntity.ok(commentService.replyComment(id, userId, request));
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable UUID id) {
        return ResponseEntity.ok(commentService.getComment(id));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<Void> updateComment(@PathVariable UUID id, @RequestBody CommentRequest request) {
        commentService.updateComment(id, authenticatedUserService.getCurrentUserId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id, authenticatedUserService.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveComment(@PathVariable UUID id) {
        commentService.approveComment(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/comments/{id}/status")
    public ResponseEntity<Void> updateCommentStatus(
            @PathVariable UUID id,
            @RequestBody CommentStatusRequest request
    ) {
        commentService.updateCommentStatus(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments/{id}/vote")
    public ResponseEntity<Void> voteComment(@PathVariable UUID id, @RequestBody VoteRequest request) {
        UUID userId = authenticatedUserService.getCurrentUserId();
        commentService.voteComment(id, userId, request);
        return ResponseEntity.ok().build();
    }
}
