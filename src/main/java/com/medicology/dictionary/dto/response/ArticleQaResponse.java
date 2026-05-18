package com.medicology.dictionary.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleQaResponse {
    private String answer;
    private List<ArticleQaCitationResponse> citations;
    private boolean outOfScope;
    private String confidence;
    private String disclaimer;
}
