package com.medicology.dictionary.service;

import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.entity.ArticleTag;
import com.medicology.dictionary.entity.Tag;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.ArticleTagRepository;
import com.medicology.dictionary.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ArticleTagRepository articleTagRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void assignTagsToArticleDeduplicatesTagIdsBeforeSave() {
        UUID articleId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(Article.builder().id(articleId).build()));
        when(tagRepository.findByIdIn(new LinkedHashSet<>(List.of(tagId, tagId))))
                .thenReturn(List.of(Tag.builder().id(tagId).build()));

        tagService.assignTagsToArticle(articleId, List.of(tagId, tagId));

        ArgumentCaptor<List<ArticleTag>> captor = ArgumentCaptor.forClass(List.class);
        verify(articleTagRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getTagId()).isEqualTo(tagId);
        verify(articleTagRepository).deleteByArticleId(articleId);
    }
}
