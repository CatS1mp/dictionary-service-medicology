package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.UserBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, UUID> {
    Optional<UserBookmark> findByUserIdAndArticleId(UUID userId, UUID articleId);
    List<UserBookmark> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    long countByArticleId(UUID articleId);
}
