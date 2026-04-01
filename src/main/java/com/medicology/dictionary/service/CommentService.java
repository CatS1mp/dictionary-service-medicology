package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.CommentRequest;
import com.medicology.dictionary.dto.request.VoteRequest;
import com.medicology.dictionary.dto.response.CommentResponse;
import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.entity.UserArticleComment;
import com.medicology.dictionary.entity.UserCommentVote;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.UserArticleCommentRepository;
import com.medicology.dictionary.repository.UserCommentVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final UserArticleCommentRepository commentRepo;
    private final ArticleRepository articleRepo;
    private final UserCommentVoteRepository voteRepo;

    @Transactional
    public UUID createComment(UUID articleId, UUID userId, CommentRequest request) {
        Article article = articleRepo.findById(articleId).orElseThrow();
        UserArticleComment comment = UserArticleComment.builder()
                .article(article)
                .userId(userId)
                .commentText(request.getCommentText())
                .isApproved(false)
                .build();
        return commentRepo.save(comment).getId();
    }

    public List<CommentResponse> getComments(UUID articleId) {
        List<UserArticleComment> comments = commentRepo.findByArticleIdAndIsApprovedTrueAndParentCommentIsNullOrderByCreatedAtDesc(articleId);
        return comments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public UUID replyComment(UUID parentId, UUID userId, CommentRequest request) {
        UserArticleComment parent = commentRepo.findById(parentId).orElseThrow();
        UserArticleComment reply = UserArticleComment.builder()
                .article(parent.getArticle())
                .userId(userId)
                .parentComment(parent)
                .commentText(request.getCommentText())
                .isApproved(false)
                .build();
        return commentRepo.save(reply).getId();
    }

    @Transactional
    public void approveComment(UUID commentId) {
        UserArticleComment comment = commentRepo.findById(commentId).orElseThrow();
        comment.setIsApproved(true);
        commentRepo.save(comment);
    }

    @Transactional
    public void voteComment(UUID commentId, UUID userId, VoteRequest request) {
        UserCommentVote vote = voteRepo.findByUserIdAndCommentId(userId, commentId).orElse(
                UserCommentVote.builder().userId(userId).commentId(commentId).build()
        );
        vote.setVoteType(request.getVoteType());
        voteRepo.save(vote);
    }

    private CommentResponse mapToResponse(UserArticleComment c) {
        CommentResponse res = new CommentResponse();
        res.setId(c.getId());
        res.setUserId(c.getUserId());
        res.setCommentText(c.getCommentText());
        res.setIsApproved(c.getIsApproved());
        res.setCreatedAt(c.getCreatedAt());
        res.setReplies(c.getReplies().stream()
                .filter(UserArticleComment::getIsApproved)
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
        return res;
    }
}
