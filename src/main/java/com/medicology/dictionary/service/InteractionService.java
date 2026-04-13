package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.response.ArticleResponse;
import com.medicology.dictionary.dto.response.InteractionSummaryResponse;
import com.medicology.dictionary.dto.response.ViewStatisticsResponse;
import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.entity.UserArticleView;
import com.medicology.dictionary.entity.UserBookmark;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.UserArticleCommentRepository;
import com.medicology.dictionary.repository.UserArticleViewRepository;
import com.medicology.dictionary.repository.UserBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class InteractionService {
    private final UserArticleViewRepository viewRepo;
    private final UserBookmarkRepository bookmarkRepo;
    private final UserArticleCommentRepository commentRepo;
    private final ArticleRepository articleRepo;
    private final ArticleService articleService;

    @Transactional
    public void recordView(UUID articleId, UUID userId) {
        ensureArticleExists(articleId);
        LocalDateTime now = LocalDateTime.now();
        viewRepo.upsertIncrementView(userId, articleId, now);
    }

    @Transactional
    public void addBookmark(UUID articleId, UUID userId) {
        Optional<UserBookmark> existing = bookmarkRepo.findByUserIdAndArticleId(userId, articleId);
        if (existing.isEmpty()) {
            Article article = ensureArticleExists(articleId);
            UserBookmark bookmark = UserBookmark.builder()
                    .article(article)
                    .userId(userId)
                    .build();
            bookmarkRepo.save(bookmark);
        }
    }

    @Transactional
    public void removeBookmark(UUID articleId, UUID userId) {
        bookmarkRepo.findByUserIdAndArticleId(userId, articleId).ifPresent(bookmarkRepo::delete);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getBookmarks(UUID userId) {
        return bookmarkRepo.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(UserBookmark::getArticle)
                .map(articleService::toResponse)
                .collect(Collectors.toList());
    }

    public InteractionSummaryResponse getInteractionSummary(UUID articleId) {
        ensureArticleExists(articleId);
        var views = viewRepo.findByArticleId(articleId);
        return InteractionSummaryResponse.builder()
                .totalViews(views.stream().mapToLong(UserArticleView::getViewCount).sum())
                .uniqueViewers(views.size())
                .totalBookmarks(bookmarkRepo.countByArticleId(articleId))
                .totalComments(commentRepo.countByArticle_Id(articleId))
                .build();
    }

    public ViewStatisticsResponse getViewStatistics(UUID articleId) {
        ensureArticleExists(articleId);
        var views = viewRepo.findByArticleId(articleId);
        return ViewStatisticsResponse.builder()
                .totalViews(views.stream().mapToLong(UserArticleView::getViewCount).sum())
                .uniqueViewers(views.size())
                .lastViewedAt(views.stream()
                        .map(UserArticleView::getLastViewedAt)
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder())
                        .orElse(null))
                .build();
    }

    private Article ensureArticleExists(UUID articleId) {
        return articleRepo.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Article not found"));
    }
}
