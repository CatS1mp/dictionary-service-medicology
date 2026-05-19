package com.medicology.dictionary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@RequiredArgsConstructor
public class ContentJsonContractValidator {
    public static final int REQUIRED_CONTENT_VERSION = 2;

    private final ObjectMapper objectMapper;

    public void validateOrThrow(String rawContentJson) {
        if (rawContentJson == null || rawContentJson.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "contentJson là bắt buộc.");
        }

        final JsonNode root;
        try {
            root = objectMapper.readTree(rawContentJson);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "contentJson must be a valid JSON object");
        }

        if (!root.isObject()) {
            throw new ResponseStatusException(BAD_REQUEST, "contentJson root must be an object");
        }

        JsonNode versionNode = root.get("version");
        if (versionNode == null || !versionNode.canConvertToInt() || versionNode.intValue() != REQUIRED_CONTENT_VERSION) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "contentJson.version must be " + REQUIRED_CONTENT_VERSION
            );
        }

        JsonNode blocksNode = root.get("blocks");
        if (blocksNode == null || !blocksNode.isArray()) {
            throw new ResponseStatusException(BAD_REQUEST, "contentJson.blocks must be an array");
        }

        for (int i = 0; i < blocksNode.size(); i += 1) {
            JsonNode block = blocksNode.get(i);
            if (!block.isObject()) {
                throw new ResponseStatusException(BAD_REQUEST, "blocks[" + i + "] must be an object");
            }

            requireNonEmptyText(block, "id", i);
            requireNonEmptyText(block, "componentCode", i);
            requireNonEmptyText(block, "componentType", i);
            requireNonEmptyText(block, "name", i);

            JsonNode levelNode = block.get("level");
            if (levelNode == null || !levelNode.canConvertToInt()) {
                throw new ResponseStatusException(BAD_REQUEST, "blocks[" + i + "].level must be an integer (1..3)");
            }
            int level = levelNode.intValue();
            if (level < 1 || level > 3) {
                throw new ResponseStatusException(BAD_REQUEST, "blocks[" + i + "].level must be in range 1..3");
            }

            JsonNode dataNode = block.get("data");
            if (dataNode == null || !dataNode.isObject()) {
                throw new ResponseStatusException(BAD_REQUEST, "blocks[" + i + "].data must be an object");
            }
        }
    }

    private void requireNonEmptyText(JsonNode block, String fieldName, int blockIndex) {
        JsonNode valueNode = block.get(fieldName);
        if (valueNode == null || !valueNode.isTextual() || valueNode.asText().trim().isEmpty()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "blocks[" + blockIndex + "]." + fieldName + " must be a non-empty string"
            );
        }
    }
}
