package com.giozar04.tags.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;
import com.giozar04.tags.infrastructure.serialization.TagMapper;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.application.exceptions.TagCreationException;
import com.giozar04.tags.application.exceptions.TagDeletionException;
import com.giozar04.tags.application.exceptions.TagRetrievalException;
import com.giozar04.tags.application.exceptions.TagUpdateException;

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
        message.addData("tag", TagMapper.toMap(tag));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_TAG");
            ServerResponseValidator.validateResponse(response);
            logger.info("Etiqueta creada exitosamente: " + response);
            return TagMapper.fromMap((Map<String, Object>) response.getData("tag"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TagCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Tag updateTagById(Long id, Tag tag) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_TAG");
        message.addData("id", id);
        message.addData("tag", TagMapper.toMap(tag));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_TAG");
            ServerResponseValidator.validateResponse(response);
            logger.info("Etiqueta actualizada correctamente: " + response);
            return TagMapper.fromMap((Map<String, Object>) response.getData("tag"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TagUpdateException("Error al esperar respuesta del servidor", e);
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
            throw new TagDeletionException("Error al esperar respuesta del servidor", e);
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
                throw new TagRetrievalException("Lista de etiquetas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Tag> tags = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        tags.add(TagMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Etiquetas obtenidas correctamente. Total: " + tags.size());
                return tags;
            } else {
                throw new TagRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TagRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Tag> getTagsByUserId(long userId) throws ClientOperationException {
        logger.info("Solicitando etiquetas del usuario " + userId + "...");
        Message message = new Message();
        message.setType("GET_TAGS_BY_USER");
        message.addData("userId", userId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_TAGS_BY_USER");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("tags");

            if (raw == null) {
                throw new TagRetrievalException("Lista de etiquetas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Tag> tags = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        tags.add(TagMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Etiquetas del usuario obtenidas correctamente. Total: " + tags.size());
                return tags;
            } else {
                throw new TagRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TagRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
