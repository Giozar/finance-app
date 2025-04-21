package com.giozar04.tags.infrastructure.handlers;

import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.tags.application.services.TagService;
import com.giozar04.tags.infrastructure.controllers.TagControllers;

public class TagHandlers implements ServerRegisterHandlers {

    private final TagService tagService;

    public TagHandlers(TagService tagService) {
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
    }
}
