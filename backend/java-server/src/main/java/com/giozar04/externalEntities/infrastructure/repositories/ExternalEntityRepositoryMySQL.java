package com.giozar04.externalEntities.infrastructure.repositories;

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
import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;
import com.giozar04.externalEntities.domain.exceptions.ExternalEntityExceptions;
import com.giozar04.externalEntities.domain.models.ExternalEntityRepositoryAbstract;

public class ExternalEntityRepositoryMySQL extends ExternalEntityRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO external_entities (user_id, name, type, contact, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM external_entities WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE external_entities SET user_id = ?, name = ?, type = ?, contact = ?, updated_at = ?
        WHERE id = ?
    """;

    private static final String SQL_DELETE = "DELETE FROM external_entities WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM external_entities";

    public ExternalEntityRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public ExternalEntity createExternalEntity(ExternalEntity entity) {
        validateExternalEntity(entity);

        if (entity.getCreatedAt() == null) entity.setCreatedAt(ZonedDateTime.now());
        if (entity.getUpdatedAt() == null) entity.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, entity.getUserId());
            stmt.setString(2, entity.getName());
            stmt.setString(3, entity.getType().getValue());
            stmt.setString(4, entity.getContact());
            stmt.setTimestamp(5, Timestamp.valueOf(entity.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(entity.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar la entidad externa");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Entidad externa creada con ID: " + entity.getId());
            return entity;

        } catch (SQLException e) {
            rollback();
            throw new ExternalEntityExceptions.ExternalEntityCreationException("Error al crear la entidad externa", e);
        }
    }

    @Override
    public ExternalEntity getExternalEntityById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExternalEntity(rs);
                } else {
                    throw new ExternalEntityExceptions.ExternalEntityNotFoundException("Entidad externa no encontrada con ID: " + id, null);
                }
            }

        } catch (SQLException e) {
            throw new ExternalEntityExceptions.ExternalEntityRetrievalException("Error al obtener la entidad externa", e);
        }
    }

    @Override
    public ExternalEntity updateExternalEntityById(long id, ExternalEntity entity) {
        validateId(id);
        validateExternalEntity(entity);
        entity.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, entity.getUserId());
            stmt.setString(2, entity.getName());
            stmt.setString(3, entity.getType().getValue());
            stmt.setString(4, entity.getContact());
            stmt.setTimestamp(5, Timestamp.valueOf(entity.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(6, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new ExternalEntityExceptions.ExternalEntityNotFoundException("Entidad externa no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            entity.setId(id);
            return entity;

        } catch (SQLException e) {
            rollback();
            throw new ExternalEntityExceptions.ExternalEntityUpdateException("Error al actualizar la entidad externa", e);
        }
    }

    @Override
    public void deleteExternalEntityById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new ExternalEntityExceptions.ExternalEntityNotFoundException("Entidad externa no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            logger.info("Entidad externa eliminada con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new ExternalEntityExceptions.ExternalEntityDeletionException("Error al eliminar la entidad externa", e);
        }
    }

    @Override
    public List<ExternalEntity> getAllExternalEntities() {
        List<ExternalEntity> entities = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                entities.add(mapResultSetToExternalEntity(rs));
            }

            return entities;

        } catch (SQLException e) {
            throw new ExternalEntityExceptions.ExternalEntityRetrievalException("Error al obtener entidades externas", e);
        }
    }

    private ExternalEntity mapResultSetToExternalEntity(ResultSet rs) throws SQLException {
        ExternalEntity entity = new ExternalEntity();
        ZoneId zone = ZoneId.systemDefault();

        entity.setId(rs.getLong("id"));
        entity.setUserId(rs.getLong("user_id"));
        entity.setName(rs.getString("name"));
        entity.setType(ExternalEntityTypes.fromValue(rs.getString("type")));
        entity.setContact(rs.getString("contact"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            entity.setCreatedAt(ZonedDateTime.of(created.toLocalDateTime(), zone));
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            entity.setUpdatedAt(ZonedDateTime.of(updated.toLocalDateTime(), zone));
        }

        return entity;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e2) {
            logger.error("Error al hacer rollback de la transacción", e2);
        }
    }
}
