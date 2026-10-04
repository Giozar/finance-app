package com.giozar04.externalEntities.application.usecases;

import java.util.List;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.externalEntities.application.ports.input.ExternalEntityOperations;
import com.giozar04.externalEntities.application.ports.output.ExternalEntityGateway;

public final class ExternalEntityUseCase implements ExternalEntityOperations {
    private final ExternalEntityGateway gateway;

    public ExternalEntityUseCase(ExternalEntityGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public ExternalEntity createExternalEntity(ExternalEntity entity) throws ClientOperationException {
        return gateway.createExternalEntity(entity);
    }

    @Override
    public ExternalEntity updateExternalEntityById(Long id, ExternalEntity entity) throws ClientOperationException {
        return gateway.updateExternalEntityById(id, entity);
    }

    @Override
    public void deleteExternalEntityById(Long id) throws ClientOperationException {
        gateway.deleteExternalEntityById(id);
    }

    @Override
    public List<ExternalEntity> getAllExternalEntities() throws ClientOperationException {
        return gateway.getAllExternalEntities();
    }

    @Override
    public List<ExternalEntity> getExternalEntitiesByUserId(long userId) throws ClientOperationException {
        return gateway.getExternalEntitiesByUserId(userId);
    }
}
