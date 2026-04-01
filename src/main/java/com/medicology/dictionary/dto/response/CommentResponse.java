package com.medicology.dictionary.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CommentResponse {
    private UUID id;
    private UUID userId;
    private String commentText;
    private Boolean isApproved;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;
}
