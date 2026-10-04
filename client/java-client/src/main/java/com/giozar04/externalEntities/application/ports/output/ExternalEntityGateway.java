package com.giozar04.externalEntities.application.ports.output;

import java.util.List;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface ExternalEntityGateway {
    ExternalEntity createExternalEntity(ExternalEntity entity) throws ClientOperationException;
    ExternalEntity updateExternalEntityById(Long id, ExternalEntity entity) throws ClientOperationException;
    void deleteExternalEntityById(Long id) throws ClientOperationException;
    List<ExternalEntity> getAllExternalEntities() throws ClientOperationException;
    List<ExternalEntity> getExternalEntitiesByUserId(long userId) throws ClientOperationException;
}
