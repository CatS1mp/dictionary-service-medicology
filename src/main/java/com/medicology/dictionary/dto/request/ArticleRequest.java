package com.medicology.dictionary.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ArticleRequest {
    private String name;
    private String slug;
    private String contentJson;
    private Integer contentVersion;
    private String contentMarkdown;
    private UUID authorAdminId;
}
