package com.medicology.dictionary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContentJsonContractValidatorTest {

    private ContentJsonContractValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ContentJsonContractValidator(new ObjectMapper());
    }

    @Test
    void acceptsValidV2Payload() {
        String validJson = """
                {
                  "version": 2,
                  "blocks": [
                    {
                      "id": "block-1",
                      "componentCode": "H2",
                      "componentType": "heading",
                      "name": "Muc 1",
                      "level": 2,
                      "data": { "content": "Noi dung" }
                    }
                  ]
                }
                """;

        assertThatNoException().isThrownBy(() -> validator.validateOrThrow(validJson));
    }

    @Test
    void rejectsMissingLevel() {
        String invalidJson = """
                {
                  "version": 2,
                  "blocks": [
                    {
                      "id": "block-1",
                      "componentCode": "P",
                      "componentType": "text",
                      "name": "Doan van",
                      "data": { "content": "Noi dung" }
                    }
                  ]
                }
                """;

        assertThatThrownBy(() -> validator.validateOrThrow(invalidJson))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("level");
    }

    @Test
    void rejectsLegacyVersion() {
        String invalidJson = """
                {
                  "version": 1,
                  "blocks": []
                }
                """;

        assertThatThrownBy(() -> validator.validateOrThrow(invalidJson))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("version must be 2");
    }
}
