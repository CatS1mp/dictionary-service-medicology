package com.medicology.dictionary.service;

import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.entity.UserArticleView;
import com.medicology.dictionary.entity.UserBookmark;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.UserArticleViewRepository;
import com.medicology.dictionary.repository.UserBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final UserArticleViewRepository viewRepo;
    private final UserBookmarkRepository bookmarkRepo;
    private final ArticleRepository articleRepo;

    @Transactional
    public void recordView(UUID articleId, UUID userId) {
        Optional<UserArticleView> existingView = viewRepo.findByUserIdAndArticleId(userId, articleId);
        if (existingView.isPresent()) {
            UserArticleView view = existingView.get();
            view.setViewCount(view.getViewCount() + 1);
            view.setLastViewedAt(LocalDateTime.now());
            viewRepo.save(view);
        } else {
            UserArticleView newView = UserArticleView.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .viewCount(1)
                    .build();
            viewRepo.save(newView);
        }
    }

    @Transactional
    public void toggleBookmark(UUID articleId, UUID userId) {
        Optional<UserBookmark> existing = bookmarkRepo.findByUserIdAndArticleId(userId, articleId);
        if (existing.isPresent()) {
            bookmarkRepo.delete(existing.get());
        } else {
            Article article = articleRepo.findById(articleId).orElseThrow();
            UserBookmark bookmark = UserBookmark.builder()
                    .article(article)
                    .userId(userId)
                    .build();
            bookmarkRepo.save(bookmark);
        }
    }
}
