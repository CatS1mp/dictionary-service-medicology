package com.medicology.dictionary.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ComponentDefinitionResponse {
    private UUID id;
    private String code;
    private String name;
    private String componentType;
    private String schemaJson;
    private String defaultDataJson;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
