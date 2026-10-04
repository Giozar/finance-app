package com.giozar04.tags.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.domain.policies.TagPolicy;
import com.giozar04.tags.application.ports.output.TagRepository;

public abstract class AbstractTagJdbcRepository implements TagRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractTagJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateTag(Tag tag) { TagPolicy.validate(tag); }

    protected void validateId(long id) { TagPolicy.validateId(id); }

    @Override
    public abstract Tag createTag(Tag tag);

    @Override
    public abstract Tag getTagById(long id);

    @Override
    public abstract Tag updateTagById(long id, Tag tag);

    @Override
    public abstract void deleteTagById(long id);

    @Override
    public abstract List<Tag> getAllTags();

    @Override
    public abstract List<Tag> getTagsByUserId(long userId);
}
