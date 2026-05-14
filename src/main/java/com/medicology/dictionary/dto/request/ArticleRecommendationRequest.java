package com.medicology.dictionary.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class ArticleRecommendationRequest {
    private List<RecommendationAttemptPayload> recentAttempts;
    private Integer limit;
}
