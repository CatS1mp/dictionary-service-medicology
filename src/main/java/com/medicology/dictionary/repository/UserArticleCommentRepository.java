package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.UserArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserArticleCommentRepository extends JpaRepository<UserArticleComment, UUID> {
    List<UserArticleComment> findByArticle_IdAndStatusAndParentCommentIsNullOrderByCreatedAtDesc(UUID articleId, String status);
    long countByArticle_Id(UUID articleId);
}
