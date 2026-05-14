package com.medicology.dictionary.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DictionaryAssetUploadResponse {
    private String assetId;
    private String url;
    private String fileName;
    private String contentType;
    private long sizeBytes;
}
