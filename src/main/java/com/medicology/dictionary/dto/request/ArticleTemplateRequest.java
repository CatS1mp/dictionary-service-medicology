package com.medicology.dictionary.dto.request;

import lombok.Data;

@Data
public class ArticleTemplateRequest {
    private String code;
    private String name;
    private String description;
    private String defaultContentJson;
    private Boolean isActive;
}
