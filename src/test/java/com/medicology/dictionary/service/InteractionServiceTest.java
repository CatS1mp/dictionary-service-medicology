package com.medicology.dictionary.service;

import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.UserArticleCommentRepository;
import com.medicology.dictionary.repository.UserArticleViewRepository;
import com.medicology.dictionary.repository.UserBookmarkRepository;
import com.medicology.dictionary.entity.UserBookmark;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {

    @Mock
    private UserArticleViewRepository viewRepo;

    @Mock
    private UserBookmarkRepository bookmarkRepo;

    @Mock
    private UserArticleCommentRepository commentRepo;

    @Mock
    private ArticleRepository articleRepo;

    @InjectMocks
    private InteractionService interactionService;

    @Test
    void removeBookmarkDeletesExistingBookmark() {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserBookmark bookmark = UserBookmark.builder().id(UUID.randomUUID()).build();

        when(bookmarkRepo.findByUserIdAndArticleId(userId, articleId)).thenReturn(Optional.of(bookmark));

        interactionService.removeBookmark(articleId, userId);

        verify(bookmarkRepo).delete(bookmark);
    }
}
