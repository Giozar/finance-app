package com.giozar04.tags.application.services;

import java.util.List;

import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.domain.interfaces.TagRepositoryInterface;

public class TagService implements TagRepositoryInterface {

    private final TagRepositoryInterface tagRepository;

    public TagService(TagRepositoryInterface tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Tag createTag(Tag tag) {
        return tagRepository.createTag(tag);
    }

    @Override
    public Tag getTagById(long id) {
        return tagRepository.getTagById(id);
    }

    @Override
    public Tag updateTagById(long id, Tag tag) {
        return tagRepository.updateTagById(id, tag);
    }

    @Override
    public void deleteTagById(long id) {
        tagRepository.deleteTagById(id);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.getAllTags();
    }
}
