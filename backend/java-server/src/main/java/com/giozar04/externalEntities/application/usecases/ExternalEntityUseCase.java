package com.giozar04.externalEntities.application.usecases;

import java.util.List;

import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.application.ports.output.ExternalEntityRepository;
import com.giozar04.externalEntities.application.ports.input.ExternalEntityOperations;

public class ExternalEntityUseCase implements ExternalEntityOperations {

    private final ExternalEntityRepository repository;

    public ExternalEntityUseCase(ExternalEntityRepository repository) {
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

    @Override
    public List<ExternalEntity> getExternalEntitiesByUserId(long userId) {
        return repository.getExternalEntitiesByUserId(userId);
    }
}
