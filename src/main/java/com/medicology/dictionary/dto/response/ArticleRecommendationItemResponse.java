package com.medicology.dictionary.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleRecommendationItemResponse {
    private UUID articleId;
    private String title;
    private String slug;
    private Double matchScore;
    private String reason;
    private String source;
    private long totalViews;
    private List<String> tags;
}
