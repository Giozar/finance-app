package com.giozar04.accounts.infrastructure.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.accounts.domain.exceptions.AccountExceptions;
import com.giozar04.accounts.domain.models.AccountRepositoryAbstract;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;

public class AccountRepositoryMySQL extends AccountRepositoryAbstract {

    private static final String SQL_INSERT_ACCOUNT = """
        INSERT INTO accounts (user_id, name, type, current_balance, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_INSERT_BANK = """
        INSERT INTO bank_details (account_id, bank_client_id, clabe, account_number, can_transfer_out, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_INSERT_CREDIT = """
        INSERT INTO credit_details (account_id, bank_client_id, credit_limit, cutoff_day, payment_deadline_day, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_UPDATE_ACCOUNT = """
        UPDATE accounts SET user_id = ?, name = ?, type = ?, current_balance = ?, updated_at = ? WHERE id = ?
    """;

    private static final String SQL_UPDATE_BANK = """
        INSERT INTO bank_details (account_id, bank_client_id, clabe, account_number, can_transfer_out, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE bank_client_id = VALUES(bank_client_id), clabe = VALUES(clabe), account_number = VALUES(account_number), can_transfer_out = VALUES(can_transfer_out), updated_at = VALUES(updated_at)
    """;

    private static final String SQL_UPDATE_CREDIT = """
        INSERT INTO credit_details (account_id, bank_client_id, credit_limit, cutoff_day, payment_deadline_day, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE bank_client_id = VALUES(bank_client_id), credit_limit = VALUES(credit_limit), cutoff_day = VALUES(cutoff_day), payment_deadline_day = VALUES(payment_deadline_day), updated_at = VALUES(updated_at)
    """;

    // Delete details on update if we switch from a type that requires them to a type that doesn't
    private static final String SQL_DELETE_BANK_DETAILS = "DELETE FROM bank_details WHERE account_id = ?";
    private static final String SQL_DELETE_CREDIT_DETAILS = "DELETE FROM credit_details WHERE account_id = ?";


    private static final String SQL_SELECT_BASE = """
        SELECT a.id, a.user_id, a.name, a.type, a.current_balance, a.created_at, a.updated_at,
               bd.bank_client_id AS bd_client_id, bd.clabe, bd.account_number, bd.can_transfer_out,
               cd.bank_client_id AS cd_client_id, cd.credit_limit, cd.cutoff_day, cd.payment_deadline_day
        FROM accounts a
        LEFT JOIN bank_details bd ON a.id = bd.account_id
        LEFT JOIN credit_details cd ON a.id = cd.account_id
    """;

    private static final String SQL_SELECT_BY_ID = SQL_SELECT_BASE + " WHERE a.id = ?";
    private static final String SQL_SELECT_ALL = SQL_SELECT_BASE;

    private static final String SQL_DELETE = "DELETE FROM accounts WHERE id = ?";

    public AccountRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public Account createAccount(Account account) {
        validateAccount(account);

        if (account.getCreatedAt() == null) account.setCreatedAt(ZonedDateTime.now());
        if (account.getUpdatedAt() == null) account.setUpdatedAt(ZonedDateTime.now());

        Timestamp createdTs = Timestamp.valueOf(account.getCreatedAt().toLocalDateTime());
        Timestamp updatedTs = Timestamp.valueOf(account.getUpdatedAt().toLocalDateTime());

        try (Connection conn = databaseConnection.getConnection()) {
            
            // 1. Insert Base Account
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ACCOUNT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, account.getUserId());
                stmt.setString(2, account.getName());
                stmt.setString(3, account.getType() != null ? account.getType().getValue() : null);
                stmt.setDouble(4, account.getCurrentBalance());
                stmt.setTimestamp(5, createdTs);
                stmt.setTimestamp(6, updatedTs);

                int affected = stmt.executeUpdate();
                if (affected == 0) throw new SQLException("No se pudo insertar en la tabla base de cuentas");

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        account.setId(keys.getLong(1));
                    }
                }
            }

            // 2. Insert Details Based on Type
            boolean isLinked = account.getType() != AccountTypes.CASH;
            boolean isCredit = account.getType() == AccountTypes.CREDIT;

            // Notice that the UI supports clabe and account_number for all linked types (BANK, WALLET, BENEFIT, e.t.c)
            if (isLinked && !isCredit) {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_BANK)) {
                    stmt.setLong(1, account.getId());
                    if (account.getBankClientId() != null) stmt.setLong(2, account.getBankClientId()); else stmt.setNull(2, Types.BIGINT);
                    stmt.setString(3, account.getClabe());
                    stmt.setString(4, account.getAccountNumber());
                    stmt.setBoolean(5, account.getCanTransferOut() != null ? account.getCanTransferOut() : true);
                    stmt.setTimestamp(6, createdTs);
                    stmt.setTimestamp(7, updatedTs);
                    stmt.executeUpdate();
                }
            }

            if (isCredit) {
                // By your UI, credit needs BankClient, Limit, Cutoff, Payment.
                try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_CREDIT)) {
                    stmt.setLong(1, account.getId());
                    if (account.getBankClientId() != null) stmt.setLong(2, account.getBankClientId()); else stmt.setNull(2, Types.BIGINT);
                    if (account.getCreditLimit() != null) stmt.setDouble(3, account.getCreditLimit()); else stmt.setNull(3, Types.DECIMAL);
                    if (account.getCutoffDay() != null) stmt.setInt(4, account.getCutoffDay()); else stmt.setNull(4, Types.INTEGER);
                    if (account.getPaymentDay() != null) stmt.setInt(5, account.getPaymentDay()); else stmt.setNull(5, Types.INTEGER);
                    stmt.setTimestamp(6, createdTs);
                    stmt.setTimestamp(7, updatedTs);
                    stmt.executeUpdate();
                }

                // If UI meant credit to ALSO have CLABE and Account Number, we could also insert into bank_details.
                // Assuming "credit_details" replaces the need for bank_details for credit cards 
                // but if we had data for CLABE let's also save it in bank details
                if (account.getAccountNumber() != null || account.getClabe() != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_BANK)) {
                        stmt.setLong(1, account.getId());
                        if (account.getBankClientId() != null) stmt.setLong(2, account.getBankClientId()); else stmt.setNull(2, Types.BIGINT);
                        stmt.setString(3, account.getClabe());
                        stmt.setString(4, account.getAccountNumber());
                        stmt.setBoolean(5, account.getCanTransferOut() != null ? account.getCanTransferOut() : true);
                        stmt.setTimestamp(6, createdTs);
                        stmt.setTimestamp(7, updatedTs);
                        stmt.executeUpdate();
                    }
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
        
        Timestamp createdTs = Timestamp.valueOf(account.getCreatedAt() != null ? account.getCreatedAt().toLocalDateTime() : ZonedDateTime.now().toLocalDateTime());
        Timestamp updatedTs = Timestamp.valueOf(account.getUpdatedAt().toLocalDateTime());

        try (Connection conn = databaseConnection.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_ACCOUNT)) {
                stmt.setLong(1, account.getUserId());
                stmt.setString(2, account.getName());
                stmt.setString(3, account.getType() != null ? account.getType().getValue() : null);
                stmt.setDouble(4, account.getCurrentBalance());
                stmt.setTimestamp(5, updatedTs);
                stmt.setLong(6, id);

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new AccountExceptions.AccountNotFoundException("Cuenta no encontrada con ID: " + id, null);
                }
            }

            boolean isLinked = account.getType() != AccountTypes.CASH;
            boolean isCredit = account.getType() == AccountTypes.CREDIT;

            // Clear details conditionally
            if (!isLinked) {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BANK_DETAILS)) { stmt.setLong(1, id); stmt.executeUpdate(); }
                try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_CREDIT_DETAILS)) { stmt.setLong(1, id); stmt.executeUpdate(); }
            }
            if (isLinked && !isCredit) {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_CREDIT_DETAILS)) { stmt.setLong(1, id); stmt.executeUpdate(); }
            }

            if (isLinked) {
                // Bank Details
                try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_BANK)) {
                    stmt.setLong(1, id);
                    if (account.getBankClientId() != null) stmt.setLong(2, account.getBankClientId()); else stmt.setNull(2, Types.BIGINT);
                    stmt.setString(3, account.getClabe());
                    stmt.setString(4, account.getAccountNumber());
                    stmt.setBoolean(5, account.getCanTransferOut() != null ? account.getCanTransferOut() : true);
                    stmt.setTimestamp(6, createdTs);
                    stmt.setTimestamp(7, updatedTs);
                    stmt.executeUpdate();
                }
            }

            if (isCredit) {
                // Credit Details
                try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_CREDIT)) {
                    stmt.setLong(1, id);
                    if (account.getBankClientId() != null) stmt.setLong(2, account.getBankClientId()); else stmt.setNull(2, Types.BIGINT);
                    if (account.getCreditLimit() != null) stmt.setDouble(3, account.getCreditLimit()); else stmt.setNull(3, Types.DECIMAL);
                    if (account.getCutoffDay() != null) stmt.setInt(4, account.getCutoffDay()); else stmt.setNull(4, Types.INTEGER);
                    if (account.getPaymentDay() != null) stmt.setInt(5, account.getPaymentDay()); else stmt.setNull(5, Types.INTEGER);
                    stmt.setTimestamp(6, createdTs);
                    stmt.setTimestamp(7, updatedTs);
                    stmt.executeUpdate();
                }
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
                try {
                    accounts.add(mapResultSetToAccount(rs));
                } catch (IllegalArgumentException e) {
                    logger.error("Omitiendo cuenta inválida (posiblemente un tipo antiguo): " + e.getMessage());
                }
            }

            return accounts;

        } catch (SQLException e) {
            throw new AccountExceptions.AccountRetrievalException("Error al obtener todas las cuentas", e);
        }
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException ex) {
            logger.error("Error al hacer rollback: " + ex.getMessage());
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("id"));
        account.setUserId(rs.getLong("user_id"));
        account.setName(rs.getString("name"));
        account.setType(AccountTypes.fromValue(rs.getString("type")));
        account.setCurrentBalance(rs.getDouble("current_balance"));

        // Dependiendo de cómo lo creamos, el bank_client_id podría estar en bd o cd.
        long bdClientId = rs.getLong("bd_client_id");
        long cdClientId = rs.getLong("cd_client_id");
        if (!rs.wasNull()) {
           account.setBankClientId(cdClientId != 0 ? cdClientId : (bdClientId != 0 ? bdClientId : null));
        }

        account.setAccountNumber(rs.getString("account_number"));
        account.setClabe(rs.getString("clabe"));

        // Optional values from bd
        boolean transfer = rs.getBoolean("can_transfer_out");
        if (!rs.wasNull()) account.setCanTransferOut(transfer);

        // Optional values from cd
        double creditLimit = rs.getDouble("credit_limit");
        if (!rs.wasNull()) account.setCreditLimit(creditLimit);

        int cutoff = rs.getInt("cutoff_day");
        if (!rs.wasNull()) account.setCutoffDay(cutoff);

        int payment = rs.getInt("payment_deadline_day");
        if (!rs.wasNull()) account.setPaymentDay(payment);

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) account.setCreatedAt(ZonedDateTime.of(createdTs.toLocalDateTime(), java.time.ZoneId.systemDefault()));

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) account.setUpdatedAt(ZonedDateTime.of(updatedTs.toLocalDateTime(), java.time.ZoneId.systemDefault()));

        return account;
    }
}
