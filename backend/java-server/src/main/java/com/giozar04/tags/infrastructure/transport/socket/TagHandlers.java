package com.giozar04.tags.infrastructure.transport.socket;

import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;
import com.giozar04.tags.application.ports.input.TagOperations;
import com.giozar04.tags.infrastructure.transport.socket.TagControllers;

public class TagHandlers implements ServerRegisterHandlers {

    private final TagOperations tagService;

    public TagHandlers(TagOperations tagService) {
        this.tagService = tagService;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            TagControllers.TagMessageTypes.CREATE_TAG,
            TagControllers.createTagController(tagService)
        );
        server.registerHandler(
            TagControllers.TagMessageTypes.GET_TAG,
            TagControllers.getTagController(tagService)
        );
        server.registerHandler(
            TagControllers.TagMessageTypes.UPDATE_TAG,
            TagControllers.updateTagController(tagService)
        );
        server.registerHandler(
            TagControllers.TagMessageTypes.DELETE_TAG,
            TagControllers.deleteTagController(tagService)
        );
        server.registerHandler(
            TagControllers.TagMessageTypes.GET_ALL_TAGS,
            TagControllers.getAllTagsController(tagService)
        );
        server.registerHandler(
            TagControllers.TagMessageTypes.GET_TAGS_BY_USER,
            TagControllers.getTagsByUserController(tagService)
        );
    }
}
