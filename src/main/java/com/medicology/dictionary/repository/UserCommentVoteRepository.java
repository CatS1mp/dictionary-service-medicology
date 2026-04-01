package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.UserCommentVote;
import com.medicology.dictionary.entity.UserCommentVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCommentVoteRepository extends JpaRepository<UserCommentVote, UserCommentVoteId> {
    Optional<UserCommentVote> findByUserIdAndCommentId(UUID userId, UUID commentId);
}
