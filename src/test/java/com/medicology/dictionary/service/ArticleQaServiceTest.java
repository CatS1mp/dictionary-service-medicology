package com.medicology.dictionary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.dictionary.config.DictionaryAiProperties;
import com.medicology.dictionary.dto.request.ArticleQaRequest;
import com.medicology.dictionary.dto.response.ArticleQaResponse;
import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.service.ai.GeminiGenerateContentClient;
import com.medicology.dictionary.service.ai.GeminiGenerateOptions;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleQaServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleContentContextBuilder contentContextBuilder;

    @Mock
    private GeminiGenerateContentClient geminiClient;

    @Mock
    private ArticleQaRateLimiter rateLimiter;

    private ArticleQaService service;

    @BeforeEach
    void setUp() {
        DictionaryAiProperties properties = new DictionaryAiProperties();
        properties.setQaEnabled(true);
        properties.setQaMaxQuestionChars(500);
        properties.setQaMaxContextChars(80_000);
        properties.setQaDailyLimitPerUser(30);

        service = new ArticleQaService(
                properties,
                articleRepository,
                contentContextBuilder,
                geminiClient,
                rateLimiter,
                new ObjectMapper());
    }

    @Test
    void rejectsUnpublishedArticle() {
        UUID articleId = UUID.randomUUID();
        Article article = Article.builder().id(articleId).isPublished(false).build();
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        ArticleQaRequest request = new ArticleQaRequest();
        request.setQuestion("Hoi gi?");

        assertThatThrownBy(() -> service.ask(UUID.randomUUID(), articleId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not published");
    }

    @Test
    void returnsOutOfScopeAnswerFromAi() {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Article article = Article.builder()
                .id(articleId)
                .name("Bai y hoc")
                .isPublished(true)
                .contentJson("{}")
                .build();

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(rateLimiter.tryConsume(userId, 30)).thenReturn(true);
        when(contentContextBuilder.buildSections(article))
                .thenReturn(List.of(new ArticleContentSection("intro", "Gioi thieu", 1, "Noi dung")));
        when(contentContextBuilder.buildPromptContext(any(), anyString(), anyInt()))
                .thenReturn("[SECTION id=\"intro\" heading=\"Gioi thieu\" level=\"1\"]\nNoi dung");
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateJsonText(anyString(), any(GeminiGenerateOptions.class)))
                .thenReturn(Optional.of("""
                        {
                          "answer": "Bai viet khong de cap chu de nay.",
                          "outOfScope": true,
                          "confidence": "high",
                          "citations": []
                        }
                        """));

        ArticleQaRequest request = new ArticleQaRequest();
        request.setQuestion("Gia Bitcoin hom nay?");

        ArticleQaResponse response = service.ask(userId, articleId, request);

        assertThat(response.isOutOfScope()).isTrue();
        assertThat(response.getCitations()).isEmpty();
        verify(geminiClient).generateJsonText(anyString(), eq(GeminiGenerateOptions.jsonDefaults()));
    }

    @Test
    void enforcesDailyRateLimit() {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        Article article = Article.builder().id(articleId).isPublished(true).build();
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(geminiClient.isConfigured()).thenReturn(true);
        when(rateLimiter.tryConsume(userId, 30)).thenReturn(false);

        ArticleQaRequest request = new ArticleQaRequest();
        request.setQuestion("Hoi gi?");

        assertThatThrownBy(() -> service.ask(userId, articleId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("gioi han");
    }
}
