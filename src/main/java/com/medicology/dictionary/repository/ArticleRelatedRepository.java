package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.ArticleRelated;
import com.medicology.dictionary.entity.ArticleRelatedId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleRelatedRepository extends JpaRepository<ArticleRelated, ArticleRelatedId> {
    List<ArticleRelated> findByArticleId(UUID articleId);
    void deleteByArticleId(UUID articleId);
    void deleteByRelatedArticleId(UUID relatedArticleId);
    void deleteByArticleIdAndRelatedArticleId(UUID articleId, UUID relatedArticleId);
}
