package com.giozar04.externalEntities.infrastructure.transport.socket;

import com.giozar04.externalEntities.application.ports.input.ExternalEntityOperations;
import com.giozar04.externalEntities.infrastructure.transport.socket.ExternalEntityControllers;
import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;

public class ExternalEntityHandlers implements ServerRegisterHandlers {

    private final ExternalEntityOperations service;

    public ExternalEntityHandlers(ExternalEntityOperations service) {
        this.service = service;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            ExternalEntityControllers.ExternalEntityMessageTypes.CREATE_EXTERNAL_ENTITY,
            ExternalEntityControllers.createExternalEntityController(service)
        );
        server.registerHandler(
            ExternalEntityControllers.ExternalEntityMessageTypes.GET_EXTERNAL_ENTITY,
            ExternalEntityControllers.getExternalEntityController(service)
        );
        server.registerHandler(
            ExternalEntityControllers.ExternalEntityMessageTypes.UPDATE_EXTERNAL_ENTITY,
            ExternalEntityControllers.updateExternalEntityController(service)
        );
        server.registerHandler(
            ExternalEntityControllers.ExternalEntityMessageTypes.DELETE_EXTERNAL_ENTITY,
            ExternalEntityControllers.deleteExternalEntityController(service)
        );
        server.registerHandler(
            ExternalEntityControllers.ExternalEntityMessageTypes.GET_ALL_EXTERNAL_ENTITIES,
            ExternalEntityControllers.getAllExternalEntitiesController(service)
        );
        server.registerHandler(
            ExternalEntityControllers.ExternalEntityMessageTypes.GET_EXTERNAL_ENTITIES_BY_USER,
            ExternalEntityControllers.getExternalEntitiesByUserController(service)
        );
    }
}
