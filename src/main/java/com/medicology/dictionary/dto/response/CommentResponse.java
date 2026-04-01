package com.medicology.dictionary.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CommentResponse {
    private UUID id;
    private UUID articleId;
    private UUID parentCommentId;
    private UUID userId;
    private String commentText;
    private Boolean isApproved;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> replies;
}
