package com.medicology.dictionary.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class RecommendationAttemptPayload {
    private String contentId;
    private String contentName;
    private List<String> tags;
    private String submittedAt;
    private Boolean passed;
}
