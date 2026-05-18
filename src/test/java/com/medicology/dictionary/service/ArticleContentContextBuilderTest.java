package com.medicology.dictionary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.dictionary.entity.Article;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleContentContextBuilderTest {

    private ArticleContentContextBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ArticleContentContextBuilder(new ObjectMapper());
    }

    @Test
    void buildsSectionsFromContentJsonV2() {
        Article article = Article.builder()
                .id(UUID.randomUUID())
                .name("Bai test")
                .contentJson("""
                        {
                          "version": 2,
                          "blocks": [
                            {
                              "id": "h1",
                              "componentCode": "H2",
                              "componentType": "heading",
                              "name": "Chan doan",
                              "level": 2,
                              "data": { "content": "Chan doan" }
                            },
                            {
                              "id": "p1",
                              "componentCode": "P",
                              "componentType": "text",
                              "name": "Doan",
                              "level": 2,
                              "data": { "content": "Trieu chung chinh la sot." }
                            }
                          ]
                        }
                        """)
                .build();

        List<ArticleContentSection> sections = builder.buildSections(article);

        assertThat(sections).isNotEmpty();
        assertThat(sections.stream().anyMatch(section -> section.plainText().contains("sot"))).isTrue();
    }

    @Test
    void fallsBackToMarkdownWhenJsonInvalid() {
        Article article = Article.builder()
                .id(UUID.randomUUID())
                .name("Bai markdown")
                .contentJson("{invalid")
                .contentMarkdown("Noi dung markdown")
                .build();

        List<ArticleContentSection> sections = builder.buildSections(article);

        assertThat(sections).hasSize(1);
        assertThat(sections.get(0).id()).isEqualTo("noi-dung");
        assertThat(sections.get(0).plainText()).contains("markdown");
    }

    @Test
    void prioritizesMatchingSectionsInPromptContext() {
        List<ArticleContentSection> sections = List.of(
                new ArticleContentSection("intro", "Gioi thieu", 1, "Noi dung mo dau"),
                new ArticleContentSection("chan-doan", "Chan doan", 2, "Trieu chung chinh la sot va ho"),
                new ArticleContentSection("ket-luan", "Ket luan", 2, "Can theo doi"));

        String context = builder.buildPromptContext(sections, "Trieu chung chinh la gi?", 50_000);

        assertThat(context).contains("Chan doan");
        assertThat(context).contains("sot");
    }
}
