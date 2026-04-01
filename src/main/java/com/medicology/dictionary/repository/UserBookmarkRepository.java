package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.UserBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBookmarkRepository extends JpaRepository<UserBookmark, UUID> {
    Optional<UserBookmark> findByUserIdAndArticleId(UUID userId, UUID articleId);
    long countByArticleId(UUID articleId);
}
