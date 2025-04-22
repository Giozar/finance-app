package com.giozar04.externalEntities.domain.interfaces;

import java.util.List;

import com.giozar04.externalEntities.domain.entities.ExternalEntity;

public interface ExternalEntityRepositoryInterface {
    ExternalEntity createExternalEntity(ExternalEntity entity);
    ExternalEntity getExternalEntityById(long id);
    ExternalEntity updateExternalEntityById(long id, ExternalEntity entity);
    void deleteExternalEntityById(long id);
    List<ExternalEntity> getAllExternalEntities();
}
