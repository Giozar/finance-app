package com.giozar04.tags.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;
import com.giozar04.tags.application.services.TagService;
import com.giozar04.tags.application.utils.TagUtils;
import com.giozar04.tags.domain.entities.Tag;

public class TagControllers {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public static final class TagMessageTypes {
        public static final String CREATE_TAG = "CREATE_TAG";
        public static final String GET_TAG = "GET_TAG";
        public static final String UPDATE_TAG = "UPDATE_TAG";
        public static final String DELETE_TAG = "DELETE_TAG";
        public static final String GET_ALL_TAGS = "GET_ALL_TAGS";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createTagController(TagService tagService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando creación de etiqueta");

            Map<String, Object> data = (Map<String, Object>) message.getData("tag");
            if (data == null) {
                return Message.createErrorMessage(TagMessageTypes.CREATE_TAG, "Datos no proporcionados");
            }

            Tag tag = TagUtils.mapToTag(data);
            Tag created = tagService.createTag(tag);

            Message response = Message.createSuccessMessage(TagMessageTypes.CREATE_TAG, "Etiqueta creada exitosamente");
            response.addData("tag", TagUtils.tagToMap(created));
            return response;
        };
    }

    public static MessageHandler getTagController(TagService tagService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de etiqueta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(TagMessageTypes.GET_TAG, "ID inválido");
            }

            Tag tag = tagService.getTagById(id);
            Message response = Message.createSuccessMessage(TagMessageTypes.GET_TAG, "Etiqueta obtenida exitosamente");
            response.addData("tag", TagUtils.tagToMap(tag));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateTagController(TagService tagService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando actualización de etiqueta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(TagMessageTypes.UPDATE_TAG, "ID inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("tag");
            if (data == null) {
                return Message.createErrorMessage(TagMessageTypes.UPDATE_TAG, "Datos no proporcionados");
            }

            Tag updated = tagService.updateTagById(id, TagUtils.mapToTag(data));

            Message response = Message.createSuccessMessage(TagMessageTypes.UPDATE_TAG, "Etiqueta actualizada exitosamente");
            response.addData("tag", TagUtils.tagToMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteTagController(TagService tagService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando eliminación de etiqueta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(TagMessageTypes.DELETE_TAG, "ID inválido");
            }

            tagService.deleteTagById(id);
            return Message.createSuccessMessage(TagMessageTypes.DELETE_TAG, "Etiqueta eliminada exitosamente");
        };
    }

    public static MessageHandler getAllTagsController(TagService tagService) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de todas las etiquetas");

            List<Tag> tags = tagService.getAllTags();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Tag t : tags) {
                result.add(TagUtils.tagToMap(t));
            }

            Message response = Message.createSuccessMessage(TagMessageTypes.GET_ALL_TAGS, "Etiquetas obtenidas exitosamente");
            response.addData("tags", result);
            response.addData("count", result.size());

            return response;
        };
    }

    private static Long parseId(Object rawId) {
        if (rawId instanceof Long l) return l;
        if (rawId instanceof String s) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
