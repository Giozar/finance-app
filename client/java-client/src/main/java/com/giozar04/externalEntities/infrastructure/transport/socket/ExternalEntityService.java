package com.giozar04.externalEntities.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.externalEntities.infrastructure.serialization.ExternalEntityMapper;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.application.exceptions.ExternalEntityCreationException;
import com.giozar04.externalEntities.application.exceptions.ExternalEntityDeletionException;
import com.giozar04.externalEntities.application.exceptions.ExternalEntityRetrievalException;
import com.giozar04.externalEntities.application.exceptions.ExternalEntityUpdateException;
import com.giozar04.externalEntities.application.ports.output.ExternalEntityGateway;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.infrastructure.transport.socket.ServerConnectionService;
import com.giozar04.serverConnection.infrastructure.transport.socket.ServerResponseValidator;

public class ExternalEntityService implements ExternalEntityGateway {

    private final ServerConnectionService serverConnectionService;
    private static final ConsoleLogger logger = ConsoleLogger.getInstance();
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
        message.addData("externalEntity", ExternalEntityMapper.toMap(entity));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_EXTERNAL_ENTITY");
            ServerResponseValidator.validateResponse(response);
            logger.info("Entidad externa creada exitosamente: " + response);
            return ExternalEntityMapper.fromMap((Map<String, Object>) response.getData("externalEntity"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalEntityCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public ExternalEntity updateExternalEntityById(Long id, ExternalEntity entity) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_EXTERNAL_ENTITY");
        message.addData("id", id);
        message.addData("externalEntity", ExternalEntityMapper.toMap(entity));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_EXTERNAL_ENTITY");
            ServerResponseValidator.validateResponse(response);
            logger.info("Entidad externa actualizada correctamente: " + response);
            return ExternalEntityMapper.fromMap((Map<String, Object>) response.getData("externalEntity"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalEntityUpdateException("Error al esperar respuesta del servidor", e);
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
            throw new ExternalEntityDeletionException("Error al esperar respuesta del servidor", e);
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
                throw new ExternalEntityRetrievalException("Lista vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<ExternalEntity> entities = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        entities.add(ExternalEntityMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Entidades externas obtenidas. Total: " + entities.size());
                return entities;
            } else {
                throw new ExternalEntityRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalEntityRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ExternalEntity> getExternalEntitiesByUserId(long userId) throws ClientOperationException {
        logger.info("Solicitando entidades externas del usuario " + userId + "...");
        Message message = new Message();
        message.setType("GET_EXTERNAL_ENTITIES_BY_USER");
        message.addData("userId", userId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_EXTERNAL_ENTITIES_BY_USER");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("externalEntities");

            if (raw == null) {
                throw new ExternalEntityRetrievalException("Lista vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<ExternalEntity> entities = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        entities.add(ExternalEntityMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Entidades externas del usuario obtenidas correctamente. Total: " + entities.size());
                return entities;
            } else {
                throw new ExternalEntityRetrievalException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalEntityRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
