package com.giozar04.categories.infrastructure.repositories;

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

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;
import com.giozar04.categories.domain.exceptions.CategoryExceptions;
import com.giozar04.categories.domain.models.CategoryRepositoryAbstract;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;

public class CategoryRepositoryMySQL extends CategoryRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO categories (user_id, name, type, icon, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM categories WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE categories SET user_id = ?, name = ?, type = ?, icon = ?, updated_at = ?
        WHERE id = ?
    """;

    private static final String SQL_DELETE = "DELETE FROM categories WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM categories";

    public CategoryRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Category createCategory(Category category) {
        validateCategory(category);

        if (category.getCreatedAt() == null) category.setCreatedAt(ZonedDateTime.now());
        if (category.getUpdatedAt() == null) category.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, category.getUserId());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getType().getValue());
            stmt.setString(4, category.getIcon());
            stmt.setTimestamp(5, Timestamp.valueOf(category.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(category.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar la categoría");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Categoría creada con ID: " + category.getId());
            return category;

        } catch (SQLException e) {
            rollback();
            throw new CategoryExceptions.CategoryCreationException("Error al crear la categoría", e);
        }
    }

    @Override
    public Category getCategoryById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                } else {
                    throw new CategoryExceptions.CategoryNotFoundException("Categoría no encontrada con ID: " + id, null);
                }
            }

        } catch (SQLException e) {
            throw new CategoryExceptions.CategoryRetrievalException("Error al obtener categoría con ID: " + id, e);
        }
    }

    @Override
    public Category updateCategoryById(long id, Category category) {
        validateId(id);
        validateCategory(category);
        category.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, category.getUserId());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getType().getValue());
            stmt.setString(4, category.getIcon());
            stmt.setTimestamp(5, Timestamp.valueOf(category.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(6, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new CategoryExceptions.CategoryNotFoundException("Categoría no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            category.setId(id);
            return category;

        } catch (SQLException e) {
            rollback();
            throw new CategoryExceptions.CategoryUpdateException("Error al actualizar categoría con ID: " + id, e);
        }
    }

    @Override
    public void deleteCategoryById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new CategoryExceptions.CategoryNotFoundException("Categoría no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            logger.info("Categoría eliminada con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new CategoryExceptions.CategoryDeletionException("Error al eliminar categoría con ID: " + id, e);
        }
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }

            return categories;

        } catch (SQLException e) {
            throw new CategoryExceptions.CategoryRetrievalException("Error al obtener todas las categorías", e);
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        ZoneId zone = ZoneId.systemDefault();

        category.setId(rs.getLong("id"));
        category.setUserId(rs.getLong("user_id"));
        category.setName(rs.getString("name"));
        category.setType(CategoryTypes.fromValue(rs.getString("type")));
        category.setIcon(rs.getString("icon"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            category.setCreatedAt(ZonedDateTime.of(created.toLocalDateTime(), zone));
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            category.setUpdatedAt(ZonedDateTime.of(updated.toLocalDateTime(), zone));
        }

        return category;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e2) {
            logger.error("Error al hacer rollback de la transacción", e2);
        }
    }
}
