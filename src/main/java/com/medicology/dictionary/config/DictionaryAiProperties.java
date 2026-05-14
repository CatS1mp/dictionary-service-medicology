package com.medicology.dictionary.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dictionary.ai")
public class DictionaryAiProperties {

    private String provider = "google-ai-studio";
    private String model = "gemini-2.5-flash";
    private String apiKey = "";
    private String endpoint = "";
    private boolean groundingEnabled = false;
    private double relevanceThreshold = 0.25d;
    private int candidateLimit = 12;
}
