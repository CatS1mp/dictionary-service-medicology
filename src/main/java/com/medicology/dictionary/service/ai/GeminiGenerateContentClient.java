package com.medicology.dictionary.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.dictionary.config.DictionaryAiProperties;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiGenerateContentClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiGenerateContentClient.class);

    private final DictionaryAiProperties aiProperties;
    private final ObjectMapper objectMapper;

    public boolean isConfigured() {
        return aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank();
    }

    public Optional<String> generateJsonText(String prompt, GeminiGenerateOptions options) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        try {
            String requestBody = buildProviderRequest(prompt, options);
            String endpoint = resolveEndpoint();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(appendApiKey(endpoint, aiProperties.getApiKey())))
                    .timeout(Duration.ofSeconds(25))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn(
                        "dictionary_gemini_provider_error status={} body={}",
                        response.statusCode(),
                        truncate(response.body(), 500));
                return Optional.empty();
            }

            JsonNode providerRoot = objectMapper.readTree(response.body());
            String aiJsonText = providerRoot.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText("");
            if (aiJsonText.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(aiJsonText);
        } catch (Exception ex) {
            log.warn("dictionary_gemini_request_failed message={}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String buildProviderRequest(String prompt, GeminiGenerateOptions options) throws Exception {
        List<Map<String, Object>> tools = new ArrayList<>();
        if (options.groundingEnabled()) {
            tools.add(Map.of("google_search", Map.of()));
        }
        Map<String, Object> requestPayload = new LinkedHashMap<>();
        requestPayload.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        requestPayload.put("generationConfig", Map.of(
                "temperature", options.temperature(),
                "responseMimeType", "application/json"));
        if (!tools.isEmpty()) {
            requestPayload.put("tools", tools);
        }
        return objectMapper.writeValueAsString(requestPayload);
    }

    private String resolveEndpoint() {
        if (aiProperties.getEndpoint() != null && !aiProperties.getEndpoint().isBlank()) {
            return aiProperties.getEndpoint().trim();
        }
        return "https://generativelanguage.googleapis.com/v1beta/models/"
                + aiProperties.getModel()
                + ":generateContent";
    }

    private String appendApiKey(String endpoint, String apiKey) {
        String delimiter = endpoint.contains("?") ? "&" : "?";
        return endpoint + delimiter + "key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
    }

    private String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, max) + "...";
    }
}
