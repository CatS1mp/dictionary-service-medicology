package com.medicology.dictionary.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ViewStatisticsResponse {
    private long totalViews;
    private long uniqueViewers;
    private LocalDateTime lastViewedAt;
}
