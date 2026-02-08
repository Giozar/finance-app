package com.giozar04.tags.infrastructure.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.tags.domain.entities.Tag;
import com.giozar04.tags.domain.exceptions.TagExceptions;
import com.giozar04.tags.domain.models.TagRepositoryAbstract;

public class TagRepositoryMySQL extends TagRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO tags (user_id, name, color, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM tags WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE tags SET user_id = ?, name = ?, color = ?, updated_at = ?
        WHERE id = ?
    """;

    private static final String SQL_DELETE = "DELETE FROM tags WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM tags";

    public TagRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Tag createTag(Tag tag) {
        validateTag(tag);

        if (tag.getCreatedAt() == null) tag.setCreatedAt(ZonedDateTime.now());
        if (tag.getUpdatedAt() == null) tag.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, tag.getUserId());
            stmt.setString(2, tag.getName());
            stmt.setString(3, tag.getColor());
            stmt.setTimestamp(4, Timestamp.valueOf(tag.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(tag.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar la etiqueta");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    tag.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Etiqueta creada con ID: " + tag.getId());
            return tag;

        } catch (SQLException e) {
            rollback();
            throw new TagExceptions.TagCreationException("Error al crear la etiqueta", e);
        }
    }

    @Override
    public Tag getTagById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTag(rs);
                } else {
                    throw new TagExceptions.TagNotFoundException("Etiqueta no encontrada con ID: " + id, null);
                }
            }

        } catch (SQLException e) {
            throw new TagExceptions.TagRetrievalException("Error al obtener la etiqueta con ID: " + id, e);
        }
    }

    @Override
    public Tag updateTagById(long id, Tag tag) {
        validateId(id);
        validateTag(tag);
        tag.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, tag.getUserId());
            stmt.setString(2, tag.getName());
            stmt.setString(3, tag.getColor());
            stmt.setTimestamp(4, Timestamp.valueOf(tag.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(5, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new TagExceptions.TagNotFoundException("Etiqueta no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            tag.setId(id);
            return tag;

        } catch (SQLException e) {
            rollback();
            throw new TagExceptions.TagUpdateException("Error al actualizar etiqueta con ID: " + id, e);
        }
    }

    @Override
    public void deleteTagById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new TagExceptions.TagNotFoundException("Etiqueta no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            logger.info("Etiqueta eliminada con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new TagExceptions.TagDeletionException("Error al eliminar etiqueta con ID: " + id, e);
        }
    }

    @Override
    public List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tags.add(mapResultSetToTag(rs));
            }

            return tags;

        } catch (SQLException e) {
            throw new TagExceptions.TagRetrievalException("Error al obtener todas las etiquetas", e);
        }
    }

    private Tag mapResultSetToTag(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        ZoneId zone = ZoneId.systemDefault();

        tag.setId(rs.getLong("id"));
        tag.setUserId(rs.getLong("user_id"));
        tag.setName(rs.getString("name"));
        tag.setColor(rs.getString("color"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            tag.setCreatedAt(ZonedDateTime.of(created.toLocalDateTime(), zone));
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            tag.setUpdatedAt(ZonedDateTime.of(updated.toLocalDateTime(), zone));
        }

        return tag;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e2) {
            logger.error("Error al hacer rollback de la transacción", e2);
        }
    }
}
