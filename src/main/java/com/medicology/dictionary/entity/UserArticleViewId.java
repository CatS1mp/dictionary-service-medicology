package com.medicology.dictionary.entity;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserArticleViewId implements Serializable {
    private UUID userId;
    private UUID articleId;
}
