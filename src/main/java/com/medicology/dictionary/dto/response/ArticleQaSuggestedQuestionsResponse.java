package com.medicology.dictionary.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleQaSuggestedQuestionsResponse {
    private List<String> questions;
}
