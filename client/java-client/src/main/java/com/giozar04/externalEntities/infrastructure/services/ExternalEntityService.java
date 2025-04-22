package com.giozar04.externalEntities.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.externalEntities.application.utils.ExternalEntityUtils;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.exceptions.ExternalEntityExceptions;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;

public class ExternalEntityService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static ExternalEntityService instance;

    private ExternalEntityService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static ExternalEntityService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new ExternalEntityService(serverConnectionService);
        }
        return instance;
    }

    public static ExternalEntityService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public ExternalEntity createExternalEntity(ExternalEntity entity) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_EXTERNAL_ENTITY");
        message.addData("externalEntity", ExternalEntityUtils.externalEntityToMap(entity));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_EXTERNAL_ENTITY");
            ServerResponseValidator.validateResponse(response);
            logger.info("Entidad externa creada exitosamente: " + response);
            return ExternalEntityUtils.mapToExternalEntity((Map<String, Object>) response.getData("externalEntity"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalEntityExceptions.ExternalEntityCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public ExternalEntity updateExternalEntityById(Long id, ExternalEntity entity) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_EXTERNAL_ENTITY");
        message.addData("id", id);
        message.addData("externalEntity", ExternalEntityUtils.externalEntityToMap(entity));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_EXTERNAL_ENTITY");
            ServerResponseValidator.validateResponse(response);
            logger.info("Entidad externa actualizada correctamente: " + response);
            return ExternalEntityUtils.mapToExternalEntity((Map<String, Object>) response.getData("externalEntity"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalEntityExceptions.ExternalEntityUpdateException("Error al esperar respuesta del servidor", e);
        }
    }

    public void deleteExternalEntityById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_EXTERNAL_ENTITY");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_EXTERNAL_ENTITY");
            ServerResponseValidator.validateResponse(response);
            logger.info("Entidad externa eliminada exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalEntityExceptions.ExternalEntityDeletionException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ExternalEntity> getAllExternalEntities() throws ClientOperationException {
        logger.info("Solicitando todas las entidades externas...");
        Message message = new Message();
        message.setType("GET_ALL_EXTERNAL_ENTITIES");

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_EXTERNAL_ENTITIES");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("externalEntities");

            if (raw == null) {
                throw new ExternalEntityExceptions.ExternalEntityRetrievalException("Lista vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<ExternalEntity> entities = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        entities.add(ExternalEntityUtils.mapToExternalEntity((Map<String, Object>) map));
                    }
                }
                logger.info("Entidades externas obtenidas. Total: " + entities.size());
                return entities;
            } else {
                throw new ExternalEntityExceptions.ExternalEntityRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalEntityExceptions.ExternalEntityRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
