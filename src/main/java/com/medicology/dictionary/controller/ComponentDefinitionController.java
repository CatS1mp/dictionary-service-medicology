package com.medicology.dictionary.controller;

import com.medicology.dictionary.dto.request.ComponentDefinitionRequest;
import com.medicology.dictionary.dto.response.ComponentDefinitionResponse;
import com.medicology.dictionary.service.ComponentDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictionary/components")
@RequiredArgsConstructor
public class ComponentDefinitionController {
    private final ComponentDefinitionService componentDefinitionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComponentDefinitionResponse> createComponent(@RequestBody ComponentDefinitionRequest request) {
        return ResponseEntity.ok(componentDefinitionService.createComponent(request));
    }

    @GetMapping
    public ResponseEntity<List<ComponentDefinitionResponse>> getAllComponents(
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly
    ) {
        return ResponseEntity.ok(componentDefinitionService.getAllComponents(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComponentDefinitionResponse> getComponentById(@PathVariable UUID id) {
        return ResponseEntity.ok(componentDefinitionService.getComponentById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateComponent(@PathVariable UUID id, @RequestBody ComponentDefinitionRequest request) {
        componentDefinitionService.updateComponent(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteComponent(@PathVariable UUID id) {
        componentDefinitionService.deleteComponent(id);
        return ResponseEntity.ok().build();
    }
}
