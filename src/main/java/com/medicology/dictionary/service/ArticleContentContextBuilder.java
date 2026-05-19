package com.medicology.dictionary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.dictionary.entity.Article;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ArticleContentContextBuilder {

    private static final int REQUIRED_CONTENT_VERSION = 2;
    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{Nd}]+");

    private final ObjectMapper objectMapper;

    public ArticleContentContextBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ArticleContentSection> buildSections(Article article) {
        String fallbackTitle = article.getName() == null || article.getName().isBlank()
                ? "Nội dung bài viết"
                : article.getName().trim();
        try {
            return buildSectionsFromJson(article.getContentJson(), fallbackTitle);
        } catch (Exception ignored) {
            String markdown = article.getContentMarkdown() == null ? "" : article.getContentMarkdown().trim();
            return List.of(new ArticleContentSection("noi-dung", fallbackTitle, 1, markdown));
        }
    }

    public String buildPromptContext(List<ArticleContentSection> sections, String question, int maxChars) {
        if (sections.isEmpty()) {
            return "";
        }
        List<ArticleContentSection> ordered = prioritizeSections(sections, question);
        StringBuilder builder = new StringBuilder();
        for (ArticleContentSection section : ordered) {
            String block = section.toPromptBlock();
            if (builder.length() + block.length() > maxChars) {
                if (builder.isEmpty()) {
                    builder.append(truncateBlock(block, maxChars));
                }
                break;
            }
            builder.append(block).append('\n');
        }
        return builder.toString().trim();
    }

    private List<ArticleContentSection> prioritizeSections(List<ArticleContentSection> sections, String question) {
        Set<String> questionTerms = tokenize(question);
        if (questionTerms.isEmpty()) {
            return sections;
        }

        List<ArticleContentSection> matched = new ArrayList<>();
        List<ArticleContentSection> rest = new ArrayList<>();
        for (ArticleContentSection section : sections) {
            Set<String> sectionTerms = tokenize(section.heading() + " " + section.plainText());
            boolean overlap = sectionTerms.stream().anyMatch(questionTerms::contains);
            if (overlap) {
                matched.add(section);
            } else {
                rest.add(section);
            }
        }
        List<ArticleContentSection> output = new ArrayList<>(matched);
        output.addAll(rest);
        return output;
    }

    private String truncateBlock(String block, int maxChars) {
        if (block.length() <= maxChars) {
            return block;
        }
        return block.substring(0, maxChars) + "\n...(truncated)";
    }

    private List<ArticleContentSection> buildSectionsFromJson(String rawJson, String fallbackTitle) throws Exception {
        if (rawJson == null || rawJson.isBlank()) {
            throw new IllegalArgumentException("contentJson is required");
        }
        JsonNode root = objectMapper.readTree(rawJson);
        if (!root.isObject()) {
            throw new IllegalArgumentException("contentJson root must be object");
        }
        int version = root.path("version").asInt(-1);
        if (version != REQUIRED_CONTENT_VERSION) {
            throw new IllegalArgumentException("unsupported contentJson version");
        }
        JsonNode blocksNode = root.get("blocks");
        if (blocksNode == null || !blocksNode.isArray()) {
            throw new IllegalArgumentException("blocks must be array");
        }

        List<ArticleContentSection> sections = new ArrayList<>();
        Map<String, Integer> sectionIdCount = new HashMap<>();
        ArticleContentSection current = createSection(fallbackTitle, 1, sectionIdCount);
        sections.add(current);

        for (JsonNode blockNode : blocksNode) {
            if (!blockNode.isObject()) {
                continue;
            }
            BlockView block = BlockView.from(blockNode);
            if (block.isHeading()) {
                current = createSection(block.headingText(), block.headingLevel(), sectionIdCount);
                sections.add(current);
                continue;
            }
            String text = block.isWarning() ? block.warningText() : block.bodyText();
            if (text != null && !text.isBlank()) {
                current = appendText(current, sections, text);
            }
        }

        return sections.stream()
                .filter(section -> !section.plainText().isBlank() || !section.heading().isBlank())
                .toList();
    }

    private ArticleContentSection createSection(String heading, int level, Map<String, Integer> sectionIdCount) {
        String normalizedHeading = heading == null || heading.isBlank() ? "Nội dung" : heading.trim();
        String baseId = slugify(normalizedHeading);
        int next = sectionIdCount.merge(baseId, 1, Integer::sum);
        String id = next == 1 ? baseId : baseId + "-" + next;
        return new ArticleContentSection(id, normalizedHeading, clampLevel(level), "");
    }

    private ArticleContentSection appendText(ArticleContentSection current, List<ArticleContentSection> sections, String text) {
        String merged = current.plainText().isBlank() ? text.trim() : current.plainText().trim() + "\n\n" + text.trim();
        ArticleContentSection updated = new ArticleContentSection(
                current.id(), current.heading(), current.headingLevel(), merged);
        sections.set(sections.size() - 1, updated);
        return updated;
    }

    private Set<String> tokenize(String value) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        if (value == null || value.isBlank()) {
            return tokens;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        for (String part : NON_WORD.split(normalized)) {
            if (part.length() >= 2) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private int clampLevel(int level) {
        if (level <= 1) {
            return 1;
        }
        if (level == 2) {
            return 2;
        }
        return 3;
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "section" : normalized;
    }

    private record BlockView(
            String componentCode,
            String componentType,
            String name,
            int level,
            JsonNode data) {

        static BlockView from(JsonNode block) {
            return new BlockView(
                    text(block, "componentCode"),
                    text(block, "componentType"),
                    text(block, "name"),
                    block.path("level").asInt(2),
                    block.get("data"));
        }

        boolean isHeading() {
            String haystack = (componentCode + " " + componentType + " " + name).toLowerCase(Locale.ROOT);
            return haystack.contains("h1")
                    || haystack.contains("h2")
                    || haystack.contains("h3")
                    || haystack.contains("heading")
                    || haystack.contains("title");
        }

        int headingLevel() {
            if (data != null && data.isObject()) {
                int fromData = data.path("headingLevel").asInt(-1);
                if (fromData < 0) {
                    fromData = data.path("rank").asInt(-1);
                }
                if (fromData < 0) {
                    fromData = data.path("level").asInt(-1);
                }
                if (fromData > 0) {
                    return fromData;
                }
            }
            String haystack = (componentCode + " " + componentType).toLowerCase(Locale.ROOT);
            if (haystack.contains("h1")) {
                return 1;
            }
            if (haystack.contains("h3")) {
                return 3;
            }
            return clampLevelStatic(level);
        }

        private static int clampLevelStatic(int level) {
            if (level <= 1) {
                return 1;
            }
            if (level == 2) {
                return 2;
            }
            return 3;
        }

        String headingText() {
            String fromData = pickFirst(data, "content", "text", "title", "heading", "label");
            if (!fromData.isBlank()) {
                return fromData;
            }
            return name.isBlank() ? componentCode : name;
        }

        boolean isWarning() {
            String haystack = (componentCode + " " + componentType + " " + name).toLowerCase(Locale.ROOT);
            return haystack.contains("warning") || haystack.contains("alert") || haystack.contains("caution");
        }

        String warningText() {
            List<String> lines = new ArrayList<>();
            String title = pickFirst(data, "title", "heading", "label");
            if (!title.isBlank()) {
                lines.add(title);
            }
            lines.addAll(flattenText(data == null ? null : data.get("items")));
            lines.addAll(flattenText(data == null ? null : data.get("list")));
            String body = pickFirst(data, "content", "text", "body");
            if (!body.isBlank()) {
                lines.add(body);
            }
            return String.join("\n", uniqueNonBlank(lines));
        }

        String bodyText() {
            List<String> lines = new ArrayList<>();
            String preferred = pickFirst(data, "content", "text", "body", "description", "lead", "summary", "title", "heading", "caption");
            if (!preferred.isBlank()) {
                lines.add(preferred);
            }
            lines.addAll(flattenText(data == null ? null : data.get("items")));
            lines.addAll(flattenText(data == null ? null : data.get("list")));
            return String.join("\n", uniqueNonBlank(lines));
        }

        private static String text(JsonNode block, String field) {
            JsonNode node = block.get(field);
            if (node == null || node.isNull()) {
                return "";
            }
            return node.asText("").trim();
        }

        private static String pickFirst(JsonNode data, String... keys) {
            if (data == null || !data.isObject()) {
                return "";
            }
            for (String key : keys) {
                JsonNode node = data.get(key);
                if (node == null || node.isNull()) {
                    continue;
                }
                if (node.isTextual() && !node.asText("").isBlank()) {
                    return node.asText("").trim();
                }
                if (node.isNumber() || node.isBoolean()) {
                    return node.asText();
                }
            }
            return "";
        }

        private static List<String> flattenText(JsonNode node) {
            List<String> lines = new ArrayList<>();
            if (node == null || node.isNull()) {
                return lines;
            }
            if (node.isTextual()) {
                for (String part : node.asText("").split("\n")) {
                    if (!part.isBlank()) {
                        lines.add(part.trim());
                    }
                }
                return lines;
            }
            if (node.isArray()) {
                for (JsonNode child : node) {
                    lines.addAll(flattenText(child));
                }
                return lines;
            }
            if (node.isObject()) {
                node.fields().forEachRemaining(entry -> lines.addAll(flattenText(entry.getValue())));
            }
            return lines;
        }

        private static List<String> uniqueNonBlank(List<String> values) {
            LinkedHashSet<String> unique = new LinkedHashSet<>();
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    unique.add(value.trim());
                }
            }
            return new ArrayList<>(unique);
        }
    }
}
