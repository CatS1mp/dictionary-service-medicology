package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.ArticleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleTemplateRepository extends JpaRepository<ArticleTemplate, UUID> {
    List<ArticleTemplate> findAllByIsActiveTrueOrderByNameAsc();
    Optional<ArticleTemplate> findByCode(String code);
}
