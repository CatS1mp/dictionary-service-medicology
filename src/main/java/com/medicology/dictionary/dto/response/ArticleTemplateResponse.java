package com.medicology.dictionary.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ArticleTemplateResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String defaultContentJson;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
