package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.ComponentDefinitionRequest;
import com.medicology.dictionary.dto.response.ComponentDefinitionResponse;
import com.medicology.dictionary.entity.ComponentDefinition;
import com.medicology.dictionary.repository.ComponentDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ComponentDefinitionService {
    private final ComponentDefinitionRepository componentDefinitionRepository;

    @Transactional
    public ComponentDefinitionResponse createComponent(ComponentDefinitionRequest request) {
        ComponentDefinition component = ComponentDefinition.builder()
                .code(request.getCode())
                .name(request.getName())
                .componentType(request.getComponentType())
                .schemaJson(request.getSchemaJson())
                .defaultDataJson(request.getDefaultDataJson())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return mapToResponse(componentDefinitionRepository.save(component));
    }

    public List<ComponentDefinitionResponse> getAllComponents(boolean activeOnly) {
        List<ComponentDefinition> components = activeOnly
                ? componentDefinitionRepository.findAllByIsActiveTrueOrderByNameAsc()
                : componentDefinitionRepository.findAll();
        return components.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ComponentDefinitionResponse getComponentById(UUID id) {
        return mapToResponse(getComponentEntity(id));
    }

    @Transactional
    public void updateComponent(UUID id, ComponentDefinitionRequest request) {
        ComponentDefinition component = getComponentEntity(id);
        component.setCode(request.getCode());
        component.setName(request.getName());
        component.setComponentType(request.getComponentType());
        component.setSchemaJson(request.getSchemaJson());
        component.setDefaultDataJson(request.getDefaultDataJson());
        component.setIsActive(request.getIsActive() != null ? request.getIsActive() : component.getIsActive());
        componentDefinitionRepository.save(component);
    }

    @Transactional
    public void deleteComponent(UUID id) {
        componentDefinitionRepository.deleteById(id);
    }

    private ComponentDefinition getComponentEntity(UUID id) {
        return componentDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy thành phần."));
    }

    private ComponentDefinitionResponse mapToResponse(ComponentDefinition component) {
        ComponentDefinitionResponse response = new ComponentDefinitionResponse();
        response.setId(component.getId());
        response.setCode(component.getCode());
        response.setName(component.getName());
        response.setComponentType(component.getComponentType());
        response.setSchemaJson(component.getSchemaJson());
        response.setDefaultDataJson(component.getDefaultDataJson());
        response.setIsActive(component.getIsActive());
        response.setCreatedAt(component.getCreatedAt());
        response.setUpdatedAt(component.getUpdatedAt());
        return response;
    }
}
