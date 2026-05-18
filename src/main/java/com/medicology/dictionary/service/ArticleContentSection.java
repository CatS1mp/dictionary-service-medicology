package com.medicology.dictionary.service;

public record ArticleContentSection(String id, String heading, int headingLevel, String plainText) {

    public String toPromptBlock() {
        return """
                [SECTION id="%s" heading="%s" level="%d"]
                %s
                """.formatted(id, escapeAttr(heading), headingLevel, plainText == null ? "" : plainText.trim());
    }

    private static String escapeAttr(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "'");
    }
}
