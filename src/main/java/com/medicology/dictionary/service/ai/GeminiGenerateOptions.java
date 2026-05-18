package com.medicology.dictionary.service.ai;

public record GeminiGenerateOptions(double temperature, boolean groundingEnabled) {

    public static GeminiGenerateOptions jsonDefaults() {
        return new GeminiGenerateOptions(0.15, false);
    }

    public static GeminiGenerateOptions recommendation(boolean groundingEnabled) {
        return new GeminiGenerateOptions(0.1, groundingEnabled);
    }
}
