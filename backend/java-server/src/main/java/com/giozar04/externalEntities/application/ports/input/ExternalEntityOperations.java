package com.giozar04.externalEntities.application.ports.input;

import java.util.List;

import com.giozar04.externalEntities.domain.entities.ExternalEntity;

public interface ExternalEntityOperations {
    ExternalEntity createExternalEntity(ExternalEntity entity);
    ExternalEntity getExternalEntityById(long id);
    ExternalEntity updateExternalEntityById(long id, ExternalEntity entity);
    void deleteExternalEntityById(long id);
    List<ExternalEntity> getAllExternalEntities();
    List<ExternalEntity> getExternalEntitiesByUserId(long userId);
}
