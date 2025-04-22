package com.giozar04.externalEntities.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;
import com.giozar04.externalEntities.domain.interfaces.ExternalEntityRepositoryInterface;
import com.giozar04.logging.CustomLogger;

public abstract class ExternalEntityRepositoryAbstract implements ExternalEntityRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected ExternalEntityRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateExternalEntity(ExternalEntity entity) {
        Objects.requireNonNull(entity, "La entidad externa no puede ser nula");

        if (entity.getName() == null || entity.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la entidad externa no puede estar vacío");
        }

        if (entity.getType() == null) {
            throw new IllegalArgumentException("El tipo de la entidad externa es obligatorio");
        }

        try {
            ExternalEntityTypes.valueOf(entity.getType().name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de entidad externa no válido: " + entity.getType());
        }

        if (entity.getContact() != null && entity.getContact().length() > 200) {
            throw new IllegalArgumentException("El campo de contacto no debe superar los 200 caracteres");
        }
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

    @Override
    public abstract ExternalEntity createExternalEntity(ExternalEntity entity);

    @Override
    public abstract ExternalEntity getExternalEntityById(long id);

    @Override
    public abstract ExternalEntity updateExternalEntityById(long id, ExternalEntity entity);

    @Override
    public abstract void deleteExternalEntityById(long id);

    @Override
    public abstract List<ExternalEntity> getAllExternalEntities();
}
