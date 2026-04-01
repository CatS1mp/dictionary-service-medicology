package com.medicology.dictionary.service;

import com.medicology.dictionary.dto.request.TagRequest;
import com.medicology.dictionary.dto.response.TagResponse;
import com.medicology.dictionary.entity.Tag;
import com.medicology.dictionary.entity.ArticleTag;
import com.medicology.dictionary.repository.TagRepository;
import com.medicology.dictionary.repository.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

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
        return tagRepository.findAll().stream().map(tag -> {
            TagResponse response = new TagResponse();
            response.setId(tag.getId());
            response.setName(tag.getName());
            response.setCreatedAt(tag.getCreatedAt());
            return response;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void assignTagsToArticle(UUID articleId, List<UUID> tagIds) {
        articleTagRepository.deleteByArticleId(articleId);
        List<ArticleTag> articleTags = tagIds.stream().map(tagId -> {
            return ArticleTag.builder().articleId(articleId).tagId(tagId).build();
        }).collect(Collectors.toList());
        articleTagRepository.saveAll(articleTags);
    }
    public TagResponse getTagById(UUID id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }

    @Transactional
    public void updateTag(UUID id, TagRequest request) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setName(request.getName());
        tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(UUID id) {
        tagRepository.deleteById(id);
    }
}
