package com.giozar04.tags.application.ports.input;

import java.util.List;

import com.giozar04.tags.domain.entities.Tag;

public interface TagOperations {
    Tag createTag(Tag tag);
    Tag getTagById(long id);
    Tag updateTagById(long id, Tag tag);
    void deleteTagById(long id);
    List<Tag> getAllTags();
    List<Tag> getTagsByUserId(long userId);
}
