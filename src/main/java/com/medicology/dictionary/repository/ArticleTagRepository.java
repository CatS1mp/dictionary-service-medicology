package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.ArticleTag;
import com.medicology.dictionary.entity.ArticleTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId> {
    List<ArticleTag> findByArticleId(UUID articleId);
    void deleteByArticleId(UUID articleId);
}
