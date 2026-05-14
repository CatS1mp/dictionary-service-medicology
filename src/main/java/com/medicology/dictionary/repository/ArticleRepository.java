package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.Article;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    Optional<Article> findBySlugAndIsPublishedTrue(String slug);
    Optional<Article> findBySlug(String slug);

    List<Article> findAllByIsPublishedTrueOrderByUpdatedAtDesc();

    @Query(
            value =
                    """
            SELECT a.*
            FROM article a
            LEFT JOIN (
                SELECT article_id, SUM(view_count) AS total_views
                FROM user_article_view
                GROUP BY article_id
            ) v ON v.article_id = a.id
            LEFT JOIN user_article_view uv ON uv.article_id = a.id AND uv.user_id = :userId
            WHERE a.is_published = true
              AND uv.user_id IS NULL
            ORDER BY COALESCE(v.total_views, 0) DESC, a.updated_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Article> findTopPublishedUnreadByViews(@Param("userId") UUID userId, @Param("limit") int limit);
}
