package com.medicology.dictionary.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InteractionSummaryResponse {
    private long totalViews;
    private long uniqueViewers;
    private long totalBookmarks;
    private long totalComments;
}
