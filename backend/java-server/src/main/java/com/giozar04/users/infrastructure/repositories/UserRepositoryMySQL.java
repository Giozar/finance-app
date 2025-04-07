package com.giozar04.users.infrastructure.repositories;

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
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.domain.exceptions.UserExceptions;
import com.giozar04.users.domain.models.UserRepositoryAbstract;

/**
 * Implementación MySQL del repositorio de usuarios. Maneja operaciones CRUD
 * para entidades User en una base de datos MySQL.
 */
public class UserRepositoryMySQL extends UserRepositoryAbstract {

    private static final String SQL_INSERT = "INSERT INTO users (name, email, password, global_balance, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String SQL_UPDATE = "UPDATE users SET name = ?, email = ?, password = ?, global_balance = ?, updated_at = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM users WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM users";

    public UserRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public User createUser(User user) {
        validateUser(user);

        // Asignar fechas si vienen nulas
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(ZonedDateTime.now());
        }
        if (user.getUpdatedAt() == null) {
            user.setUpdatedAt(ZonedDateTime.now());
        }

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setDouble(4, user.getGlobalBalance());
            stmt.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(user.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("No se pudo insertar el usuario.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Usuario creado con ID: " + user.getId());
            return user;

        } catch (SQLException e) {
            rollback();
            throw new UserExceptions.UserCreationException("Error al crear el usuario", e);
        }
    }

    @Override
    public User getUserById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                } else {
                    throw new UserExceptions.UserNotFoundException("Usuario no encontrado con ID: " + id, null);
                }
            }

        } catch (SQLException e) {
            throw new UserExceptions.UserRetrievalException("Error al obtener usuario con ID: " + id, e);
        }
    }

    @Override
    public User updateUserById(long id, User user) {
        validateId(id);
        validateUser(user);

        // Se actualiza updatedAt, incluso si no viene
        if (user.getUpdatedAt() == null) {
            user.setUpdatedAt(ZonedDateTime.now());
        }

        user.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setDouble(4, user.getGlobalBalance());
            stmt.setTimestamp(5, Timestamp.valueOf(user.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(6, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new UserExceptions.UserNotFoundException("Usuario no encontrado con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            user.setId(id);
            return user;

        } catch (SQLException e) {
            rollback();
            throw new UserExceptions.UserUpdateException("Error al actualizar usuario con ID: " + id, e);
        }
    }

    @Override
    public void deleteUserById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new UserExceptions.UserNotFoundException("Usuario no encontrado con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            logger.info("Usuario eliminado con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new UserExceptions.UserDeletionException("Error al eliminar usuario con ID: " + id, e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new UserExceptions.UserRetrievalException("Error al obtener todos los usuarios", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setGlobalBalance(rs.getDouble("global_balance"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");

        ZoneId zone = ZoneId.systemDefault();
        if (createdTs != null) {
            user.setCreatedAt(ZonedDateTime.of(createdTs.toLocalDateTime(), zone));
        }
        if (updatedTs != null) {
            user.setUpdatedAt(ZonedDateTime.of(updatedTs.toLocalDateTime(), zone));
        }

        return user;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e2) {
            logger.error("Error al hacer rollback de la transacción", e2);
        }
    }
}
