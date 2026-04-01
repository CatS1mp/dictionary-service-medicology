package com.medicology.dictionary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_article_view")
@IdClass(UserArticleViewId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserArticleView {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "article_id")
    private UUID articleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 1;

    @CreationTimestamp
    @Column(name = "first_viewed_at", updatable = false)
    private LocalDateTime firstViewedAt;

    @UpdateTimestamp
    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;
}
