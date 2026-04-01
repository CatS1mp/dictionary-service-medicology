package com.medicology.dictionary.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ArticleResponse {
    private UUID id;
    private UUID themeId;
    private String name;
    private String slug;
    private String contentMarkdown;
    private UUID authorAdminId;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagResponse> tags;
}
