package com.giozar04.externalEntities.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionInterface;
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;
import com.giozar04.externalEntities.application.ports.output.ExternalEntityRepository;
import com.giozar04.externalEntities.domain.policies.ExternalEntityPolicy;
import com.giozar04.logging.infrastructure.ConsoleLogger;

public abstract class AbstractExternalEntityJdbcRepository implements ExternalEntityRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractExternalEntityJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateExternalEntity(ExternalEntity entity) {
        ExternalEntityPolicy.validateExternalEntity(entity);
    }

    protected void validateId(long id) {
        ExternalEntityPolicy.validateId(id);
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

    @Override
    public abstract List<ExternalEntity> getExternalEntitiesByUserId(long userId);
}
