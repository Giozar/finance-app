package com.giozar04.externalEntities.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.externalEntities.application.services.ExternalEntityService;
import com.giozar04.externalEntities.application.utils.ExternalEntityUtils;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;

public class ExternalEntityControllers {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public static final class ExternalEntityMessageTypes {
        public static final String CREATE_EXTERNAL_ENTITY = "CREATE_EXTERNAL_ENTITY";
        public static final String GET_EXTERNAL_ENTITY = "GET_EXTERNAL_ENTITY";
        public static final String UPDATE_EXTERNAL_ENTITY = "UPDATE_EXTERNAL_ENTITY";
        public static final String DELETE_EXTERNAL_ENTITY = "DELETE_EXTERNAL_ENTITY";
        public static final String GET_ALL_EXTERNAL_ENTITIES = "GET_ALL_EXTERNAL_ENTITIES";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createExternalEntityController(ExternalEntityService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando creación de entidad externa");

            Map<String, Object> data = (Map<String, Object>) message.getData("externalEntity");
            if (data == null) {
                return Message.createErrorMessage(ExternalEntityMessageTypes.CREATE_EXTERNAL_ENTITY, "Datos no proporcionados");
            }

            ExternalEntity entity = ExternalEntityUtils.mapToExternalEntity(data);
            ExternalEntity created = service.createExternalEntity(entity);

            Message response = Message.createSuccessMessage(ExternalEntityMessageTypes.CREATE_EXTERNAL_ENTITY, "Entidad externa creada exitosamente");
            response.addData("externalEntity", ExternalEntityUtils.externalEntityToMap(created));
            return response;
        };
    }

    public static MessageHandler getExternalEntityController(ExternalEntityService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de entidad externa");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(ExternalEntityMessageTypes.GET_EXTERNAL_ENTITY, "ID inválido");
            }

            ExternalEntity entity = service.getExternalEntityById(id);
            Message response = Message.createSuccessMessage(ExternalEntityMessageTypes.GET_EXTERNAL_ENTITY, "Entidad externa obtenida");
            response.addData("externalEntity", ExternalEntityUtils.externalEntityToMap(entity));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateExternalEntityController(ExternalEntityService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando actualización de entidad externa");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(ExternalEntityMessageTypes.UPDATE_EXTERNAL_ENTITY, "ID inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("externalEntity");
            if (data == null) {
                return Message.createErrorMessage(ExternalEntityMessageTypes.UPDATE_EXTERNAL_ENTITY, "Datos no proporcionados");
            }

            ExternalEntity updated = service.updateExternalEntityById(id, ExternalEntityUtils.mapToExternalEntity(data));

            Message response = Message.createSuccessMessage(ExternalEntityMessageTypes.UPDATE_EXTERNAL_ENTITY, "Entidad externa actualizada");
            response.addData("externalEntity", ExternalEntityUtils.externalEntityToMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteExternalEntityController(ExternalEntityService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando eliminación de entidad externa");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(ExternalEntityMessageTypes.DELETE_EXTERNAL_ENTITY, "ID inválido");
            }

            service.deleteExternalEntityById(id);
            return Message.createSuccessMessage(ExternalEntityMessageTypes.DELETE_EXTERNAL_ENTITY, "Entidad externa eliminada");
        };
    }

    public static MessageHandler getAllExternalEntitiesController(ExternalEntityService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Procesando obtención de todas las entidades externas");

            List<ExternalEntity> entities = service.getAllExternalEntities();
            List<Map<String, Object>> result = new ArrayList<>();

            for (ExternalEntity e : entities) {
                result.add(ExternalEntityUtils.externalEntityToMap(e));
            }

            Message response = Message.createSuccessMessage(ExternalEntityMessageTypes.GET_ALL_EXTERNAL_ENTITIES, "Entidades externas obtenidas");
            response.addData("externalEntities", result);
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
