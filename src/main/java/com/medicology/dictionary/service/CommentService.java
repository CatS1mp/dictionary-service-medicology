package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.CommentRequest;
import com.medicology.dictionary.dto.request.CommentStatusRequest;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CommentService {
    private static final String APPROVED = "APPROVED";
    private static final String PENDING = "PENDING";
    private static final String REJECTED = "REJECTED";
    private static final String HIDDEN = "HIDDEN";

    private final UserArticleCommentRepository commentRepo;
    private final ArticleRepository articleRepo;
    private final UserCommentVoteRepository voteRepo;

    @Transactional
    public UUID createComment(UUID articleId, UUID userId, CommentRequest request) {
        Article article = articleRepo.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Article not found"));
        UserArticleComment comment = UserArticleComment.builder()
                .article(article)
                .userId(userId)
                .commentText(request.getCommentText())
                .isApproved(false)
                .status(PENDING)
                .build();
        return commentRepo.save(comment).getId();
    }

    public List<CommentResponse> getComments(UUID articleId) {
        List<UserArticleComment> comments = commentRepo
                .findByArticle_IdAndStatusAndParentCommentIsNullOrderByCreatedAtDesc(articleId, APPROVED);
        return comments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public CommentResponse getComment(UUID commentId) {
        return mapToResponse(getCommentEntity(commentId));
    }

    @Transactional
    public UUID replyComment(UUID parentId, UUID userId, CommentRequest request) {
        UserArticleComment parent = getCommentEntity(parentId);
        UserArticleComment reply = UserArticleComment.builder()
                .article(parent.getArticle())
                .userId(userId)
                .parentComment(parent)
                .commentText(request.getCommentText())
                .isApproved(false)
                .status(PENDING)
                .build();
        return commentRepo.save(reply).getId();
    }

    @Transactional
    public void approveComment(UUID commentId) {
        setCommentStatus(commentId, APPROVED);
    }

    @Transactional
    public void updateComment(UUID commentId, UUID userId, CommentRequest request) {
        UserArticleComment comment = getCommentEntity(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "User cannot update this comment");
        }
        comment.setCommentText(request.getCommentText());
        commentRepo.save(comment);
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        UserArticleComment comment = getCommentEntity(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "User cannot delete this comment");
        }
        commentRepo.delete(comment);
    }

    @Transactional
    public void updateCommentStatus(UUID commentId, CommentStatusRequest request) {
        setCommentStatus(commentId, request.getStatus());
    }

    @Transactional
    public void voteComment(UUID commentId, UUID userId, VoteRequest request) {
        getCommentEntity(commentId);
        UserCommentVote vote = voteRepo.findByUserIdAndCommentId(userId, commentId).orElse(
                UserCommentVote.builder().userId(userId).commentId(commentId).build()
        );
        vote.setVoteType(request.getVoteType());
        voteRepo.save(vote);
    }

    private CommentResponse mapToResponse(UserArticleComment c) {
        CommentResponse res = new CommentResponse();
        res.setId(c.getId());
        res.setArticleId(c.getArticle().getId());
        res.setParentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null);
        res.setUserId(c.getUserId());
        res.setCommentText(c.getCommentText());
        res.setIsApproved(c.getIsApproved());
        res.setStatus(c.getStatus());
        res.setCreatedAt(c.getCreatedAt());
        res.setUpdatedAt(c.getUpdatedAt());
        res.setReplies(c.getReplies().stream()
                .filter(reply -> APPROVED.equals(reply.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
        return res;
    }

    private UserArticleComment getCommentEntity(UUID commentId) {
        return commentRepo.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Comment not found"));
    }

    private void setCommentStatus(UUID commentId, String rawStatus) {
        String status = normalizeStatus(rawStatus);
        UserArticleComment comment = getCommentEntity(commentId);
        comment.setStatus(status);
        comment.setIsApproved(APPROVED.equals(status));
        commentRepo.save(comment);
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Comment status is required");
        }

        String status = rawStatus.trim().toUpperCase(Locale.ROOT);
        if (!List.of(APPROVED, PENDING, REJECTED, HIDDEN).contains(status)) {
            throw new ResponseStatusException(BAD_REQUEST, "Unsupported comment status");
        }
        return status;
    }
}
