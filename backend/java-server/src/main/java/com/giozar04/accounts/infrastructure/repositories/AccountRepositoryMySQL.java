package com.giozar04.accounts.infrastructure.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.exceptions.AccountExceptions;
import com.giozar04.accounts.domain.models.AccountRepositoryAbstract;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;

public class AccountRepositoryMySQL extends AccountRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO accounts (user_id, bank_client_id, name, type, current_balance, bank_name,
        account_number, clabe, credit_limit, cutoff_day, payment_day, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM accounts WHERE id = ?";
    private static final String SQL_UPDATE = """
        UPDATE accounts SET user_id = ?, bank_client_id = ?, name = ?, type = ?, current_balance = ?,
        bank_name = ?, account_number = ?, clabe = ?, credit_limit = ?, cutoff_day = ?, payment_day = ?,
        updated_at = ? WHERE id = ?
    """;
    private static final String SQL_DELETE = "DELETE FROM accounts WHERE id = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM accounts";

    public AccountRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Account createAccount(Account account) {
        validateAccount(account);

        if (account.getCreatedAt() == null) {
            account.setCreatedAt(ZonedDateTime.now());
        }
        if (account.getUpdatedAt() == null) {
            account.setUpdatedAt(ZonedDateTime.now());
        }

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, account.getUserId());
            if (account.getBankClientId() != null) {
                stmt.setLong(2, account.getBankClientId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setString(3, account.getName());
            stmt.setString(4, account.getType());
            stmt.setDouble(5, account.getCurrentBalance());
            stmt.setString(6, account.getBankName());
            stmt.setString(7, account.getAccountNumber());
            stmt.setString(8, account.getClabe());
            if (account.getCreditLimit() != null) {
                stmt.setDouble(9, account.getCreditLimit());
            } else {
                stmt.setNull(9, Types.DOUBLE);
            }
            if (account.getCutoffDay() != null) {
                stmt.setInt(10, account.getCutoffDay());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            if (account.getPaymentDay() != null) {
                stmt.setInt(11, account.getPaymentDay());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }
            stmt.setTimestamp(12, Timestamp.valueOf(account.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(13, Timestamp.valueOf(account.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar la cuenta");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setId(keys.getLong(1));
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Cuenta creada con ID: " + account.getId());
            return account;

        } catch (SQLException e) {
            rollback();
            throw new AccountExceptions.AccountCreationException("Error al crear la cuenta", e);
        }
    }

    @Override
    public Account getAccountById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                } else {
                    throw new AccountExceptions.AccountNotFoundException("Cuenta no encontrada con ID: " + id, null);
                }
            }

        } catch (SQLException e) {
            throw new AccountExceptions.AccountRetrievalException("Error al obtener cuenta con ID: " + id, e);
        }
    }

    @Override
    public Account updateAccountById(long id, Account account) {
        validateId(id);
        validateAccount(account);

        account.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setLong(1, account.getUserId());
            if (account.getBankClientId() != null) {
                stmt.setLong(2, account.getBankClientId());
            } else {
                stmt.setNull(2, Types.BIGINT);
            }
            stmt.setString(3, account.getName());
            stmt.setString(4, account.getType());
            stmt.setDouble(5, account.getCurrentBalance());
            stmt.setString(6, account.getBankName());
            stmt.setString(7, account.getAccountNumber());
            stmt.setString(8, account.getClabe());
            if (account.getCreditLimit() != null) {
                stmt.setDouble(9, account.getCreditLimit());
            } else {
                stmt.setNull(9, Types.DOUBLE);
            }
            if (account.getCutoffDay() != null) {
                stmt.setInt(10, account.getCutoffDay());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            if (account.getPaymentDay() != null) {
                stmt.setInt(11, account.getPaymentDay());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }
            stmt.setTimestamp(12, Timestamp.valueOf(account.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(13, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new AccountExceptions.AccountNotFoundException("Cuenta no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            account.setId(id);
            return account;

        } catch (SQLException e) {
            rollback();
            throw new AccountExceptions.AccountUpdateException("Error al actualizar cuenta con ID: " + id, e);
        }
    }

    @Override
    public void deleteAccountById(long id) {
        validateId(id);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new AccountExceptions.AccountNotFoundException("Cuenta no encontrada con ID: " + id, null);
            }

            databaseConnection.commitTransaction();
            logger.info("Cuenta eliminada con ID: " + id);

        } catch (SQLException e) {
            rollback();
            throw new AccountExceptions.AccountDeletionException("Error al eliminar cuenta con ID: " + id, e);
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }

            return accounts;

        } catch (SQLException e) {
            throw new AccountExceptions.AccountRetrievalException("Error al obtener todas las cuentas", e);
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("id"));
        account.setUserId(rs.getLong("user_id"));

        long bankClientId = rs.getLong("bank_client_id");
        if (!rs.wasNull()) account.setBankClientId(bankClientId);

        account.setName(rs.getString("name"));
        account.setType(rs.getString("type"));
        account.setCurrentBalance(rs.getDouble("current_balance"));
        account.setBankName(rs.getString("bank_name"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setClabe(rs.getString("clabe"));

        double creditLimit = rs.getDouble("credit_limit");
        if (!rs.wasNull()) account.setCreditLimit(creditLimit);

        int cutoff = rs.getInt("cutoff_day");
        if (!rs.wasNull()) account.setCutoffDay(cutoff);

        int payment = rs.getInt("payment_day");
        if (!rs.wasNull()) account.setPaymentDay(payment);

        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");

        ZoneId zone = ZoneId.systemDefault();
        if (createdTs != null) {
            account.setCreatedAt(ZonedDateTime.of(createdTs.toLocalDateTime(), zone));
        }
        if (updatedTs != null) {
            account.setUpdatedAt(ZonedDateTime.of(updatedTs.toLocalDateTime(), zone));
        }

        return account;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e2) {
            logger.error("Error al hacer rollback de la transacción", e2);
        }
    }
}
