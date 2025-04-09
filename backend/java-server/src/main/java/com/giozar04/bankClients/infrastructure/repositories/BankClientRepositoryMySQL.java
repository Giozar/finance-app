package com.giozar04.bankClients.infrastructure.repositories;

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

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClient.domain.exceptions.BankClientExceptions;
import com.giozar04.bankClients.domain.models.BankClientRepositoryAbstract;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;

public class BankClientRepositoryMySQL extends BankClientRepositoryAbstract {

    private static final String SQL_INSERT = "INSERT INTO bank_clients (user_id, bank_name, client_number, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM bank_clients WHERE id = ?";
    private static final String SQL_SELECT_BY_USER_ID = "SELECT * FROM bank_clients WHERE user_id = ?";
    private static final String SQL_UPDATE = "UPDATE bank_clients SET user_id = ?, bank_name = ?, client_number = ?, updated_at = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM bank_clients WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM bank_clients";

    public BankClientRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public BankClient createBankClient(BankClient bankClient) {
        validateBankClient(bankClient);

        // Asignar timestamps si no vienen
        if (bankClient.getCreatedAt() == null) {
            bankClient.setCreatedAt(ZonedDateTime.now());
        }
        if (bankClient.getUpdatedAt() == null) {
            bankClient.setUpdatedAt(ZonedDateTime.now());
        }

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, bankClient.getUserId());
            stmt.setString(2, bankClient.getBankName());
            stmt.setString(3, bankClient.getClientNumber());
            stmt.setTimestamp(4, Timestamp.valueOf(bankClient.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(bankClient.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("No se pudo insertar el cliente bancario");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    bankClient.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("BankClient creado con ID: " + bankClient.getId());
            return bankClient;

        } catch (SQLException e) {
            rollback();
            throw new BankClientExceptions.BankClientCreationException("Error al crear el cliente bancario", e);
        }
    }

    @Override
    public BankClient getBankClientById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBankClient(rs);
                } else {
                    throw new BankClientExceptions.BankClientNotFoundException("Cliente bancario no encontrado", null);
                }
            }

        } catch (SQLException e) {
            throw new BankClientExceptions.BankClientRetrievalException("Error al buscar el cliente bancario", e);
        }
    }

    @Override
    public List<BankClient> getBankClientsByUserId(long userId) {
        validateId(userId);

        List<BankClient> clients = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_USER_ID)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapResultSetToBankClient(rs));
                }
            }

            return clients;

        } catch (SQLException e) {
            throw new BankClientExceptions.BankClientRetrievalException("Error al obtener clientes por usuario", e);
        }
    }

    @Override
    public BankClient updateBankClientById(long id, BankClient bankClient) {
        validateId(id);
        validateBankClient(bankClient);

        // Se actualiza updatedAt, incluso si no viene
        if (bankClient.getUpdatedAt() == null) {
            bankClient.setUpdatedAt(ZonedDateTime.now());
        }

        bankClient.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, bankClient.getUserId());  // NUEVO
            stmt.setString(2, bankClient.getBankName());
            stmt.setString(3, bankClient.getClientNumber());
            stmt.setTimestamp(4, Timestamp.valueOf(bankClient.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(5, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new BankClientExceptions.BankClientNotFoundException("Cliente bancario no encontrado para actualizar", null);
            }

            databaseConnection.commitTransaction();
            bankClient.setId(id);
            return bankClient;

        } catch (SQLException e) {
            rollback();
            throw new BankClientExceptions.BankClientUpdateException("Error al actualizar el cliente bancario", e);
        }

    }

    @Override
    public void deleteBankClientById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new BankClientExceptions.BankClientNotFoundException("Cliente bancario no encontrado para eliminar", null);
            }

            databaseConnection.commitTransaction();
            logger.info("BankClient eliminado con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new BankClientExceptions.BankClientDeletionException("Error al eliminar el cliente bancario", e);
        }
    }

    @Override
    public List<BankClient> getAllBankClients() {
        List<BankClient> clients = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clients.add(mapResultSetToBankClient(rs));
            }

            return clients;

        } catch (SQLException e) {
            throw new BankClientExceptions.BankClientRetrievalException("Error al obtener todos los clientes", e);
        }
    }

    private BankClient mapResultSetToBankClient(ResultSet rs) throws SQLException {
        ZoneId zone = ZoneId.systemDefault();

        return new BankClient(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("bank_name"),
                rs.getString("client_number"),
                rs.getTimestamp("created_at").toLocalDateTime().atZone(zone),
                rs.getTimestamp("updated_at").toLocalDateTime().atZone(zone)
        );
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e2) {
            logger.error("Error durante rollback", e2);
        }
    }
}
