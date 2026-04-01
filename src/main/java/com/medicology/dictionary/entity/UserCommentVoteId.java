package com.medicology.dictionary.entity;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCommentVoteId implements Serializable {
    private UUID userId;
    private UUID commentId;
}
