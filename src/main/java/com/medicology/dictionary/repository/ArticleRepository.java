package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    Optional<Article> findBySlugAndIsPublishedTrue(String slug);
    Optional<Article> findBySlug(String slug);

    List<Article> findAllByIsPublishedTrueOrderByUpdatedAtDesc();
}
