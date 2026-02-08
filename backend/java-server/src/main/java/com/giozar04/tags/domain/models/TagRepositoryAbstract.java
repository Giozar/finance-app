package com.giozar04.tags.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.domain.interfaces.TagRepositoryInterface;

public abstract class TagRepositoryAbstract implements TagRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected TagRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateTag(Tag tag) {
        Objects.requireNonNull(tag, "La etiqueta no puede ser nula");

        if (tag.getName() == null || tag.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío");
        }

        if (tag.getColor() == null || tag.getColor().isBlank()) {
            throw new IllegalArgumentException("El color de la etiqueta no puede estar vacío");
        }
        if (tag.getUserId() <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido para la etiqueta");
        }
    }


    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

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
}
