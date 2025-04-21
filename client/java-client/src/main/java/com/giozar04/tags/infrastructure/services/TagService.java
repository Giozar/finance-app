package com.giozar04.tags.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;
import com.giozar04.tags.application.utils.TagUtils;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.domain.exceptions.TagExceptions;

public class TagService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static TagService instance;

    private TagService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static TagService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new TagService(serverConnectionService);
        }
        return instance;
    }

    public static TagService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public Tag createTag(Tag tag) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_TAG");
        message.addData("tag", TagUtils.tagToMap(tag));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_TAG");
            ServerResponseValidator.validateResponse(response);
            logger.info("Etiqueta creada exitosamente: " + response);
            return TagUtils.mapToTag((Map<String, Object>) response.getData("tag"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TagExceptions.TagCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Tag updateTagById(Long id, Tag tag) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_TAG");
        message.addData("id", id);
        message.addData("tag", TagUtils.tagToMap(tag));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_TAG");
            ServerResponseValidator.validateResponse(response);
            logger.info("Etiqueta actualizada correctamente: " + response);
            return TagUtils.mapToTag((Map<String, Object>) response.getData("tag"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TagExceptions.TagUpdateException("Error al esperar respuesta del servidor", e);
        }
    }

    public void deleteTagById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_TAG");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_TAG");
            ServerResponseValidator.validateResponse(response);
            logger.info("Etiqueta eliminada exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TagExceptions.TagDeletionException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Tag> getAllTags() throws ClientOperationException {
        logger.info("Solicitando todas las etiquetas...");
        Message message = new Message();
        message.setType("GET_ALL_TAGS");

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_TAGS");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("tags");

            if (raw == null) {
                throw new TagExceptions.TagRetrievalException("Lista de etiquetas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Tag> tags = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        tags.add(TagUtils.mapToTag((Map<String, Object>) map));
                    }
                }
                logger.info("Etiquetas obtenidas correctamente. Total: " + tags.size());
                return tags;
            } else {
                throw new TagExceptions.TagRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TagExceptions.TagRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
