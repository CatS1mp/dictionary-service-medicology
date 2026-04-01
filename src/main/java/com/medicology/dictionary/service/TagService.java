package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.TagRequest;
import com.medicology.dictionary.dto.response.TagResponse;
import com.medicology.dictionary.entity.Tag;
import com.medicology.dictionary.entity.ArticleTag;
import com.medicology.dictionary.repository.ArticleRepository;
import com.medicology.dictionary.repository.TagRepository;
import com.medicology.dictionary.repository.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public TagResponse createTag(TagRequest request) {
        Tag tag = Tag.builder().name(request.getName()).build();
        tag = tagRepository.save(tag);
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void assignTagsToArticle(UUID articleId, List<UUID> tagIds) {
        articleRepository.findById(articleId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Article not found"));
        Set<UUID> distinctTagIds = new LinkedHashSet<>(tagIds);
        List<Tag> tags = tagRepository.findByIdIn(distinctTagIds);
        if (tags.size() != distinctTagIds.size()) {
            throw new ResponseStatusException(BAD_REQUEST, "One or more tags do not exist");
        }

        articleTagRepository.deleteByArticleId(articleId);
        List<ArticleTag> articleTags = distinctTagIds.stream()
                .map(tagId -> ArticleTag.builder().articleId(articleId).tagId(tagId).build())
                .collect(Collectors.toList());
        articleTagRepository.saveAll(articleTags);
    }

    public TagResponse getTagById(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tag not found"));
        return mapToResponse(tag);
    }

    @Transactional
    public void updateTag(UUID id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tag not found"));
        tag.setName(request.getName());
        tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        articleTagRepository.deleteByTagId(id);
        tagRepository.deleteById(id);
    }

    public List<TagResponse> getTagsByArticle(UUID articleId) {
        return articleTagRepository.findByArticleId(articleId).stream()
                .map(ArticleTag::getTag)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeTagFromArticle(UUID articleId, UUID tagId) {
        articleTagRepository.deleteByArticleIdAndTagId(articleId, tagId);
    }

    private TagResponse mapToResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }
}
