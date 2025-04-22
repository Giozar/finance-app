package com.giozar04.externalEntities.infrastructure.handlers;

import com.giozar04.externalEntities.application.services.ExternalEntityService;
import com.giozar04.externalEntities.infrastructure.controllers.ExternalEntityControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class ExternalEntityHandlers implements ServerRegisterHandlers {

    private final ExternalEntityService service;

    public ExternalEntityHandlers(ExternalEntityService service) {
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
    }
}
