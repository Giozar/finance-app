package com.giozar04.tags.domain.interfaces;

import java.util.List;

import com.giozar04.tags.domain.entities.Tag;

public interface TagRepositoryInterface {
    Tag createTag(Tag tag);
    Tag getTagById(long id);
    Tag updateTagById(long id, Tag tag);
    void deleteTagById(long id);
    List<Tag> getAllTags();
}
