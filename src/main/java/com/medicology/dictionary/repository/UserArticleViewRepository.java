package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.UserArticleView;
import com.medicology.dictionary.entity.UserArticleViewId;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserArticleViewRepository extends JpaRepository<UserArticleView, UserArticleViewId> {
    Optional<UserArticleView> findByUserIdAndArticleId(UUID userId, UUID articleId);
    List<UserArticleView> findByArticleId(UUID articleId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_article_view (user_id, article_id, view_count, first_viewed_at, last_viewed_at) "
            + "VALUES (:userId, :articleId, 1, :now, :now) "
            + "ON CONFLICT (user_id, article_id) DO UPDATE SET "
            + "view_count = user_article_view.view_count + 1, "
            + "last_viewed_at = EXCLUDED.last_viewed_at",
            nativeQuery = true)
    void upsertIncrementView(@Param("userId") UUID userId, @Param("articleId") UUID articleId, @Param("now") LocalDateTime now);
}
