package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.ArticleTemplateRequest;
import com.medicology.dictionary.dto.response.ArticleTemplateResponse;
import com.medicology.dictionary.entity.ArticleTemplate;
import com.medicology.dictionary.repository.ArticleTemplateRepository;
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
public class ArticleTemplateService {
    private final ArticleTemplateRepository articleTemplateRepository;

    @Transactional
    public ArticleTemplateResponse createTemplate(ArticleTemplateRequest request) {
        ArticleTemplate template = ArticleTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .defaultContentJson(request.getDefaultContentJson())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return mapToResponse(articleTemplateRepository.save(template));
    }

    public List<ArticleTemplateResponse> getAllTemplates(boolean activeOnly) {
        List<ArticleTemplate> templates = activeOnly
                ? articleTemplateRepository.findAllByIsActiveTrueOrderByNameAsc()
                : articleTemplateRepository.findAll();
        return templates.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ArticleTemplateResponse getTemplateById(UUID id) {
        return mapToResponse(getTemplateEntity(id));
    }

    @Transactional
    public void updateTemplate(UUID id, ArticleTemplateRequest request) {
        ArticleTemplate template = getTemplateEntity(id);
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setDefaultContentJson(request.getDefaultContentJson());
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : template.getIsActive());
        articleTemplateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        articleTemplateRepository.deleteById(id);
    }

    private ArticleTemplate getTemplateEntity(UUID id) {
        return articleTemplateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy mẫu bài viết."));
    }

    private ArticleTemplateResponse mapToResponse(ArticleTemplate template) {
        ArticleTemplateResponse response = new ArticleTemplateResponse();
        response.setId(template.getId());
        response.setCode(template.getCode());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setDefaultContentJson(template.getDefaultContentJson());
        response.setIsActive(template.getIsActive());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }
}
