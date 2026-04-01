package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.CommentStatusRequest;
import com.medicology.dictionary.entity.UserArticleComment;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.UserArticleCommentRepository;
import com.medicology.dictionary.repository.UserCommentVoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private UserArticleCommentRepository commentRepo;

    @Mock
    private ArticleRepository articleRepo;

    @Mock
    private UserCommentVoteRepository voteRepo;

    @InjectMocks
    private CommentService commentService;

    @Test
    void updateCommentStatusApprovesCommentAndSetsApprovedFlag() {
        UUID commentId = UUID.randomUUID();
        UserArticleComment comment = UserArticleComment.builder()
                .id(commentId)
                .isApproved(false)
                .status("PENDING")
                .build();
        CommentStatusRequest request = new CommentStatusRequest();
        request.setStatus("approved");

        when(commentRepo.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.updateCommentStatus(commentId, request);

        ArgumentCaptor<UserArticleComment> captor = ArgumentCaptor.forClass(UserArticleComment.class);
        verify(commentRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("APPROVED");
        assertThat(captor.getValue().getIsApproved()).isTrue();
    }
}
