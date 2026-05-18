package com.medicology.dictionary.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class ArticleQaRequest {
    private String question;
    private List<ArticleQaConversationMessage> conversation;
}
