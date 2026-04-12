package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.TagRequest;
import com.medicology.dictionary.dto.response.TagResponse;
import com.medicology.dictionary.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponse> createTag(@RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable UUID id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTag(@PathVariable UUID id, @RequestBody TagRequest request) {
        tagService.updateTag(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok().build();
    }
}
