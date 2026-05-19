package com.medicology.dictionary.service;

import com.medicology.dictionary.config.DictionaryAssetProperties;
import com.medicology.dictionary.dto.response.DictionaryAssetUploadResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DictionaryAssetService {
    private final DictionaryAssetProperties assetProperties;
    private final DictionarySupabaseAssetUploader supabaseAssetUploader;

    public DictionaryAssetUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Cần tải lên tệp tin.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!assetProperties.getAllowedTypes().contains(contentType)) {
            throw new ResponseStatusException(BAD_REQUEST, "Loại tệp không được hỗ trợ: " + contentType);
        }
        if (file.getSize() > assetProperties.getMaxSizeBytes()) {
            throw new ResponseStatusException(BAD_REQUEST, "Kích thước tệp vượt giới hạn cho phép.");
        }

        String assetId = UUID.randomUUID().toString();
        String extension = resolveExtension(file.getOriginalFilename(), contentType);
        String safeFileName = assetId + extension;

        if (supabaseAssetUploader.isConfigured()) {
            String objectPrefix = sanitizeObjectPrefix(assetProperties.getSupabase().getObjectPrefix());
            String objectKey = objectPrefix + "/" + safeFileName;
            String publicUrl = supabaseAssetUploader.uploadToPublicBucket(file, objectKey);
            return DictionaryAssetUploadResponse.builder()
                    .assetId(assetId)
                    .fileName(objectKey)
                    .url(publicUrl)
                    .contentType(contentType)
                    .sizeBytes(file.getSize())
                    .build();
        }

        Path destination = resolveStorageDirectory().resolve(safeFileName).normalize();

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Không lưu được tệp đã tải lên.");
        }

        return DictionaryAssetUploadResponse.builder()
                .assetId(assetId)
                .fileName(safeFileName)
                .url(assetProperties.getBaseUrl().replaceAll("/+$", "") + "/" + safeFileName)
                .contentType(contentType)
                .sizeBytes(file.getSize())
                .build();
    }

    private static String sanitizeObjectPrefix(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "dictionary-assets";
        }
        String trimmed = raw.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        if (!trimmed.matches("^[A-Za-z0-9][A-Za-z0-9._-]*(/[A-Za-z0-9._-]+)*$")) {
            return "dictionary-assets";
        }
        return trimmed;
    }

    public Resource loadAsResource(String fileName) {
        if (!StringUtils.hasText(fileName) || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ResponseStatusException(BAD_REQUEST, "Tên tệp không hợp lệ.");
        }
        Path candidate = resolveStorageDirectory().resolve(fileName).normalize();
        try {
            if (!Files.exists(candidate) || !Files.isRegularFile(candidate)) {
                throw new ResponseStatusException(NOT_FOUND, "Không tìm thấy tệp.");
            }
            Resource resource = new UrlResource(candidate.toUri());
            if (!resource.exists()) {
                throw new ResponseStatusException(NOT_FOUND, "Không tìm thấy tệp.");
            }
            return resource;
        } catch (IOException ex) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Không đọc được tệp đã lưu.");
        }
    }

    public String detectContentType(String fileName) {
        Path candidate = resolveStorageDirectory().resolve(fileName).normalize();
        try {
            String detected = Files.probeContentType(candidate);
            return detected == null ? "application/octet-stream" : detected;
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }

    private Path resolveStorageDirectory() {
        return Paths.get(assetProperties.getStoragePath()).toAbsolutePath().normalize();
    }

    private String resolveExtension(String originalFileName, String contentType) {
        if (StringUtils.hasText(originalFileName) && originalFileName.contains(".")) {
            String ext = originalFileName.substring(originalFileName.lastIndexOf('.')).toLowerCase();
            if (ext.matches("\\.[a-z0-9]{1,8}")) {
                return ext;
            }
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".bin";
        };
    }
}
