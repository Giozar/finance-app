package com.giozar04.tags.application.usecases;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.application.ports.input.TagOperations;
import com.giozar04.tags.application.ports.output.TagGateway;

public final class TagUseCase implements TagOperations {
    private final TagGateway gateway;

    public TagUseCase(TagGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public Tag createTag(Tag tag) throws ClientOperationException {
        return gateway.createTag(tag);
    }

    @Override
    public Tag updateTagById(Long id, Tag tag) throws ClientOperationException {
        return gateway.updateTagById(id, tag);
    }

    @Override
    public void deleteTagById(Long id) throws ClientOperationException {
        gateway.deleteTagById(id);
    }

    @Override
    public List<Tag> getAllTags() throws ClientOperationException {
        return gateway.getAllTags();
    }

    @Override
    public List<Tag> getTagsByUserId(long userId) throws ClientOperationException {
        return gateway.getTagsByUserId(userId);
    }
}
