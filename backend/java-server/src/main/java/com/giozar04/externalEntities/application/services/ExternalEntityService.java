package com.giozar04.externalEntities.application.services;

import java.util.List;

import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.interfaces.ExternalEntityRepositoryInterface;

public class ExternalEntityService implements ExternalEntityRepositoryInterface {

    private final ExternalEntityRepositoryInterface repository;

    public ExternalEntityService(ExternalEntityRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public ExternalEntity createExternalEntity(ExternalEntity entity) {
        return repository.createExternalEntity(entity);
    }

    @Override
    public ExternalEntity getExternalEntityById(long id) {
        return repository.getExternalEntityById(id);
    }

    @Override
    public ExternalEntity updateExternalEntityById(long id, ExternalEntity entity) {
        return repository.updateExternalEntityById(id, entity);
    }

    @Override
    public void deleteExternalEntityById(long id) {
        repository.deleteExternalEntityById(id);
    }

    @Override
    public List<ExternalEntity> getAllExternalEntities() {
        return repository.getAllExternalEntities();
    }
}
