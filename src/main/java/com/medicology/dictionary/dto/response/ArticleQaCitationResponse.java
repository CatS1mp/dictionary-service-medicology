package com.medicology.dictionary.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleQaCitationResponse {
    private String sectionId;
    private String heading;
    private String quote;
}
