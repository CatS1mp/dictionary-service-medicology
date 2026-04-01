package com.medicology.dictionary.entity;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRelatedId implements Serializable {
    private UUID articleId;
    private UUID relatedArticleId;
}
