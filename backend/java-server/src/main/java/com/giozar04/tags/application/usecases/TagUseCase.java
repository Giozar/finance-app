package com.giozar04.tags.application.usecases;

import java.util.List;

import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.application.ports.output.TagRepository;
import com.giozar04.tags.application.ports.input.TagOperations;
import com.giozar04.tags.domain.policies.TagPolicy;

public class TagUseCase implements TagOperations {

    private final TagRepository tagRepository;

    public TagUseCase(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Tag createTag(Tag tag) {
        TagPolicy.validate(tag);
        return tagRepository.createTag(tag);
    }

    @Override
    public Tag getTagById(long id) {
        TagPolicy.validateId(id);
        return tagRepository.getTagById(id);
    }

    @Override
    public Tag updateTagById(long id, Tag tag) {
        TagPolicy.validateId(id);
        TagPolicy.validate(tag);
        return tagRepository.updateTagById(id, tag);
    }

    @Override
    public void deleteTagById(long id) {
        TagPolicy.validateId(id);
        tagRepository.deleteTagById(id);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagRepository.getAllTags();
    }

    @Override
    public List<Tag> getTagsByUserId(long userId) {
        TagPolicy.validateId(userId);
        return tagRepository.getTagsByUserId(userId);
    }
}
