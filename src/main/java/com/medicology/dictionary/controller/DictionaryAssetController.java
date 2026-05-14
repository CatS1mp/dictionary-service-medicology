package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.response.DictionaryAssetUploadResponse;
import com.medicology.dictionary.service.DictionaryAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DictionaryAssetController {
    private final DictionaryAssetService dictionaryAssetService;

    @PostMapping("/admin/assets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DictionaryAssetUploadResponse> uploadAsset(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(dictionaryAssetService.upload(file));
    }

    @GetMapping("/assets/{fileName}")
    public ResponseEntity<Resource> getAsset(@PathVariable String fileName) {
        Resource resource = dictionaryAssetService.loadAsResource(fileName);
        String contentType = dictionaryAssetService.detectContentType(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
