package com.giozar04.tags.application.ports.input;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.tags.domain.entities.Tag;

public interface TagOperations {
    Tag createTag(Tag tag) throws ClientOperationException;
    Tag updateTagById(Long id, Tag tag) throws ClientOperationException;
    void deleteTagById(Long id) throws ClientOperationException;
    List<Tag> getAllTags() throws ClientOperationException;
    List<Tag> getTagsByUserId(long userId) throws ClientOperationException;
}
