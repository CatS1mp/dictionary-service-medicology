package com.medicology.dictionary.repository;

import com.medicology.dictionary.entity.UserArticleView;
import com.medicology.dictionary.entity.UserArticleViewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserArticleViewRepository extends JpaRepository<UserArticleView, UserArticleViewId> {
    Optional<UserArticleView> findByUserIdAndArticleId(UUID userId, UUID articleId);
    List<UserArticleView> findByArticleId(UUID articleId);
}
