package com.medicology.dictionary.service;

import com.medicology.dictionary.config.DictionaryAssetProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Uploads dictionary media to Supabase Storage (public bucket) using the service role key.
 */
@Component
@RequiredArgsConstructor
public class DictionarySupabaseAssetUploader {
    private final RestTemplateBuilder restTemplateBuilder;
    private final DictionaryAssetProperties assetProperties;

    public String uploadToPublicBucket(MultipartFile file, String objectPathRelativeToBucket) {
        DictionaryAssetProperties.Supabase cfg = assetProperties.getSupabase();
        if (!isConfigured(cfg)) {
            throw new IllegalStateException("Supabase storage is not configured");
        }

        String supabaseUrl = normalizeBaseUrl(cfg.getUrl());
        String bucket = normalizeBucketName(cfg.getBucket());
        String serviceRoleKey = cfg.getServiceRoleKey().trim();

        MediaType contentType = resolveContentType(file.getContentType());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.set("x-upsert", "true");

        RestTemplate restTemplate = restTemplateBuilder.build();
        String encodedPath = encodeBucketAndPath(bucket, objectPathRelativeToBucket);
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + encodedPath;

        try {
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Supabase did not accept the upload");
            }

            return supabaseUrl + "/storage/v1/object/public/" + encodedPath;
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(
                    INTERNAL_SERVER_ERROR,
                    "Supabase upload failed: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
        } catch (IOException | RestClientException ex) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Supabase upload failed: " + ex.getMessage());
        }
    }

    public boolean isConfigured() {
        return isConfigured(assetProperties.getSupabase());
    }

    private boolean isConfigured(DictionaryAssetProperties.Supabase cfg) {
        return cfg != null
                && StringUtils.hasText(cfg.getUrl())
                && StringUtils.hasText(cfg.getBucket())
                && StringUtils.hasText(cfg.getServiceRoleKey());
    }

    private static String normalizeBaseUrl(String raw) {
        String t = raw.trim();
        return t.endsWith("/") ? t.substring(0, t.length() - 1) : t;
    }

    private static String normalizeBucketName(String configuredBucket) {
        String trimmedBucket = Optional.ofNullable(configuredBucket)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalArgumentException("dictionary.assets.supabase.bucket must not be blank"));

        String normalizedBucket = trimmedBucket
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-");

        if (!normalizedBucket.matches("^[a-z0-9][a-z0-9._-]*$")) {
            throw new IllegalArgumentException(
                    "dictionary.assets.supabase.bucket must be a valid Supabase bucket id, for example 'dictionary-media'");
        }

        return normalizedBucket;
    }

    private static String encodeBucketAndPath(String bucket, String objectPath) {
        String combined = bucket + "/" + objectPath;
        return UriUtils.encodePath(combined, StandardCharsets.UTF_8);
    }

    private static MediaType resolveContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
