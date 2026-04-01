package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.CommentRequest;
import com.medicology.dictionary.dto.request.VoteRequest;
import com.medicology.dictionary.dto.response.CommentResponse;
import com.medicology.dictionary.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/articles/{articleId}/comments")
    public ResponseEntity<UUID> createComment(@PathVariable UUID articleId, @RequestBody CommentRequest request) {
        UUID userId = UUID.randomUUID(); 
        return ResponseEntity.ok(commentService.createComment(articleId, userId, request));
    }

    @GetMapping("/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID articleId) {
        return ResponseEntity.ok(commentService.getComments(articleId));
    }

    @PostMapping("/comments/{id}/reply")
    public ResponseEntity<UUID> replyComment(@PathVariable UUID id, @RequestBody CommentRequest request) {
        UUID userId = UUID.randomUUID();
        return ResponseEntity.ok(commentService.replyComment(id, userId, request));
    }

    @PostMapping("/comments/{id}/approve")
    public ResponseEntity<Void> approveComment(@PathVariable UUID id) {
        commentService.approveComment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments/{id}/vote")
    public ResponseEntity<Void> voteComment(@PathVariable UUID id, @RequestBody VoteRequest request) {
        UUID userId = UUID.randomUUID();
        commentService.voteComment(id, userId, request);
        return ResponseEntity.ok().build();
    }
}
