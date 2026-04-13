package com.medicology.dictionary.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VoteRequest {
    @Pattern(regexp = "^(UP|DOWN)$", message = "voteType must be UP or DOWN")
    private String voteType;
}
