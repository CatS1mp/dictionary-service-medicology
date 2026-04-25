package com.medicology.dictionary.dto.request;

import lombok.Data;

@Data
public class ComponentDefinitionRequest {
    private String code;
    private String name;
    private String componentType;
    private String schemaJson;
    private String defaultDataJson;
    private Boolean isActive;
}
