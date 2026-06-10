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

    // -- SAVINGS --
    private static final String SQL_INSERT_SAVINGS = """
        INSERT INTO savings_details (account_id, annual_yield, yield_cap_amount, last_yield_calculation, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_UPDATE_SAVINGS = """
        INSERT INTO savings_details (account_id, annual_yield, yield_cap_amount, last_yield_calculation, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE annual_yield = VALUES(annual_yield), yield_cap_amount = VALUES(yield_cap_amount),
        last_yield_calculation = VALUES(last_yield_calculation), updated_at = VALUES(updated_at)
    """;

    // -- INVESTMENT --
    private static final String SQL_INSERT_INVESTMENT = """
        INSERT INTO investment_details
            (account_id, instrument_type, term_days, principal_amount, annual_yield,
             day_count_basis, start_date, maturity_date, status, auto_reinvest,
             reinvest_term_days, reinvest_annual_yield, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String SQL_UPDATE_INVESTMENT = """
        UPDATE investment_details
        SET instrument_type       = ?,
            term_days             = ?,
            principal_amount      = ?,
            annual_yield          = ?,
            day_count_basis       = ?,
            start_date            = ?,
            maturity_date         = ?,
            status                = ?,
            auto_reinvest         = ?,
            reinvest_term_days    = ?,
            reinvest_annual_yield = ?,
            updated_at            = ?
        WHERE account_id = ?
    """;


    private static final String SQL_DELETE_BANK_DETAILS      = "DELETE FROM bank_details WHERE account_id = ?";
    private static final String SQL_DELETE_CREDIT_DETAILS    = "DELETE FROM credit_details WHERE account_id = ?";
    private static final String SQL_DELETE_SAVINGS_DETAILS   = "DELETE FROM savings_details WHERE account_id = ?";
    private static final String SQL_DELETE_INVESTMENT_DETAILS = "DELETE FROM investment_details WHERE account_id = ?";


    private static final String SQL_SELECT_BASE = """
        SELECT a.id, a.user_id, a.name, a.type, a.current_balance, a.created_at, a.updated_at,
               bd.bank_client_id AS bd_client_id, bd.clabe, bd.account_number, bd.can_transfer_out,
               cd.bank_client_id AS cd_client_id, cd.credit_limit, cd.credit_used, cd.cutoff_day, cd.payment_deadline_day,
               sd.annual_yield, sd.yield_cap_amount, sd.last_yield_calculation,
               inv.instrument_type, inv.term_days, inv.principal_amount,
               inv.annual_yield AS inv_annual_yield, inv.day_count_basis,
               inv.start_date, inv.maturity_date, inv.status AS inv_status,
               inv.auto_reinvest, inv.reinvest_term_days, inv.reinvest_annual_yield
        FROM accounts a
        LEFT JOIN bank_details bd ON a.id = bd.account_id
        LEFT JOIN credit_details cd ON a.id = cd.account_id
        LEFT JOIN savings_details sd ON a.id = sd.account_id
        LEFT JOIN investment_details inv ON a.id = inv.account_id
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
            boolean isLinked = account.getType() != AccountTypes.CASH
                            && account.getType() != AccountTypes.SAVINGS
                            && account.getType() != AccountTypes.INVESTMENT;
            boolean isCredit = account.getType() == AccountTypes.CREDIT;
            boolean isSavings = account.getType() == AccountTypes.SAVINGS;
            boolean isInvestment = account.getType() == AccountTypes.INVESTMENT;

            // Determinar si debe guardar en bank_details (DEBIT, CREDIT, WALLET, BENEFIT)
            if (isLinked) {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_BANK)) {
                    stmt.setLong(1, account.getId());
                    if (account.getBankClientId() != null) stmt.setLong(2, account.getBankClientId()); else stmt.setNull(2, Types.BIGINT);
                    stmt.setString(3, account.getClabe());
                    stmt.setString(4, account.getAccountNumber());
                    // BENEFIT = vales (can_transfer_out=false por defecto), DEBIT = true
                    boolean canTransfer = account.getType() == AccountTypes.BENEFIT
                        ? (account.getCanTransferOut() != null ? account.getCanTransferOut() : false)
                        : (account.getCanTransferOut() != null ? account.getCanTransferOut() : true);
                    stmt.setBoolean(5, canTransfer);
                    stmt.setTimestamp(6, createdTs);
                    stmt.setTimestamp(7, updatedTs);
                    stmt.executeUpdate();
                }
            }

            if (isCredit) {
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
            }

            if (isSavings) {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_SAVINGS)) {
                    stmt.setLong(1, account.getId());
                    stmt.setDouble(2, account.getAnnualYield());
                    if (account.getYieldCapAmount() != null) stmt.setDouble(3, account.getYieldCapAmount()); else stmt.setNull(3, Types.DECIMAL);
                    
                    String lyc = account.getLastYieldCalculation();
                    if (lyc != null && !lyc.trim().isEmpty()) stmt.setDate(4, java.sql.Date.valueOf(lyc.trim())); else stmt.setNull(4, Types.DATE);
                    
                    stmt.setTimestamp(5, createdTs);
                    stmt.setTimestamp(6, updatedTs);
                    stmt.executeUpdate();
                }
            }

            isInvestment = account.getType() == AccountTypes.INVESTMENT;
            if (isInvestment) {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_INVESTMENT)) {
                    stmt.setLong(1, account.getId());
                    stmt.setString(2, account.getInstrumentType());
                    if (account.getTermDays() != null) stmt.setInt(3, account.getTermDays()); else stmt.setNull(3, Types.INTEGER);
                    stmt.setDouble(4, account.getPrincipalAmount());
                    stmt.setDouble(5, account.getInvestmentAnnualYield());
                    stmt.setInt(6, account.getDayCountBasis() != null ? account.getDayCountBasis() : 360);
                    
                    String sd = account.getStartDate();
                    if (sd != null && !sd.trim().isEmpty()) stmt.setDate(7, java.sql.Date.valueOf(sd.trim())); else stmt.setNull(7, Types.DATE);
                    
                    String md = account.getMaturityDate();
                    if (md != null && !md.trim().isEmpty()) stmt.setDate(8, java.sql.Date.valueOf(md.trim())); else stmt.setNull(8, Types.DATE);
                    
                    stmt.setString(9, account.getInvestmentStatus() != null ? account.getInvestmentStatus() : "ACTIVE");
                    stmt.setBoolean(10, account.getAutoReinvest() != null ? account.getAutoReinvest() : false);
                    if (account.getReinvestTermDays() != null) stmt.setInt(11, account.getReinvestTermDays()); else stmt.setNull(11, Types.INTEGER);
                    if (account.getReinvestAnnualYield() != null) stmt.setDouble(12, account.getReinvestAnnualYield()); else stmt.setNull(12, Types.DECIMAL);
                    stmt.setTimestamp(13, createdTs);
                    stmt.setTimestamp(14, updatedTs);
                    stmt.executeUpdate();
                }
            }

            databaseConnection.commitTransaction();
            logger.info("Cuenta creada con ID: " + account.getId());
            return account;

        } catch (SQLException e) {
            logger.error("Error al crear cuenta: " + e.getMessage(), e);
            rollback();
            throw new AccountExceptions.AccountCreationException("Error al crear la cuenta", e);
        } catch (IllegalArgumentException e) {
            logger.error("Error de formato (probablemente de fecha) al crear cuenta: " + e.getMessage(), e);
            rollback();
            throw new AccountExceptions.AccountCreationException("Error en el formato de los datos", e);
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

            boolean isLinked = account.getType() != AccountTypes.CASH
                            && account.getType() != AccountTypes.SAVINGS
                            && account.getType() != AccountTypes.INVESTMENT;
            boolean isCredit = account.getType() == AccountTypes.CREDIT;
            boolean isSavings = account.getType() == AccountTypes.SAVINGS;
            boolean isInvestment = account.getType() == AccountTypes.INVESTMENT;

            // Limpiar detalles que ya no aplican si el tipo cambió
            if (!isLinked) {
                try (PreparedStatement s = conn.prepareStatement(SQL_DELETE_BANK_DETAILS)) { s.setLong(1, id); s.executeUpdate(); }
            }
            if (!isCredit) {
                try (PreparedStatement s = conn.prepareStatement(SQL_DELETE_CREDIT_DETAILS)) { s.setLong(1, id); s.executeUpdate(); }
            }
            if (!isSavings) {
                try (PreparedStatement s = conn.prepareStatement(SQL_DELETE_SAVINGS_DETAILS)) { s.setLong(1, id); s.executeUpdate(); }
            }
            if (!isInvestment) {
                try (PreparedStatement s = conn.prepareStatement(SQL_DELETE_INVESTMENT_DETAILS)) { s.setLong(1, id); s.executeUpdate(); }
            }

            if (isLinked) {
                // Bank Details (DEBIT, CREDIT, WALLET, BENEFIT)
                try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_BANK)) {
                    stmt.setLong(1, id);
                    if (account.getBankClientId() != null) stmt.setLong(2, account.getBankClientId()); else stmt.setNull(2, Types.BIGINT);
                    stmt.setString(3, account.getClabe());
                    stmt.setString(4, account.getAccountNumber());
                    boolean canTransfer = account.getType() == AccountTypes.BENEFIT
                        ? (account.getCanTransferOut() != null ? account.getCanTransferOut() : false)
                        : (account.getCanTransferOut() != null ? account.getCanTransferOut() : true);
                    stmt.setBoolean(5, canTransfer);
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

            if (isSavings) {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_SAVINGS)) {
                    stmt.setLong(1, id);
                    stmt.setDouble(2, account.getAnnualYield());
                    if (account.getYieldCapAmount() != null) stmt.setDouble(3, account.getYieldCapAmount()); else stmt.setNull(3, Types.DECIMAL);
                    
                    String lyc = account.getLastYieldCalculation();
                    if (lyc != null && !lyc.trim().isEmpty()) stmt.setDate(4, java.sql.Date.valueOf(lyc.trim())); else stmt.setNull(4, Types.DATE);
                    
                    stmt.setTimestamp(5, createdTs);
                    stmt.setTimestamp(6, updatedTs);
                    stmt.executeUpdate();
                }
            }

            if (isInvestment) {
                try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_INVESTMENT)) {
                    stmt.setString(1, account.getInstrumentType());
                    if (account.getTermDays() != null) stmt.setInt(2, account.getTermDays()); else stmt.setNull(2, Types.INTEGER);
                    stmt.setDouble(3, account.getPrincipalAmount());
                    stmt.setDouble(4, account.getInvestmentAnnualYield());
                    stmt.setInt(5, account.getDayCountBasis() != null ? account.getDayCountBasis() : 360);

                    String sd = account.getStartDate();
                    if (sd != null && !sd.trim().isEmpty()) stmt.setDate(6, java.sql.Date.valueOf(sd.trim())); else stmt.setNull(6, Types.DATE);

                    String md = account.getMaturityDate();
                    if (md != null && !md.trim().isEmpty()) stmt.setDate(7, java.sql.Date.valueOf(md.trim())); else stmt.setNull(7, Types.DATE);

                    stmt.setString(8, account.getInvestmentStatus() != null ? account.getInvestmentStatus() : "ACTIVE");
                    stmt.setBoolean(9, account.getAutoReinvest() != null ? account.getAutoReinvest() : false);
                    if (account.getReinvestTermDays() != null) stmt.setInt(10, account.getReinvestTermDays()); else stmt.setNull(10, Types.INTEGER);
                    if (account.getReinvestAnnualYield() != null) stmt.setDouble(11, account.getReinvestAnnualYield()); else stmt.setNull(11, Types.DECIMAL);
                    stmt.setTimestamp(12, updatedTs);
                    stmt.setLong(13, id);  // WHERE account_id = ?
                    stmt.executeUpdate();
                }
            }

            databaseConnection.commitTransaction();
            account.setId(id);
            return account;

        } catch (SQLException e) {
            logger.error("Error al actualizar cuenta con ID " + id + ": " + e.getMessage(), e);
            rollback();
            throw new AccountExceptions.AccountUpdateException("Error al actualizar cuenta con ID: " + id, e);
        } catch (IllegalArgumentException e) {
            logger.error("Error de formato (probablemente de fecha) al actualizar cuenta con ID " + id + ": " + e.getMessage(), e);
            rollback();
            throw new AccountExceptions.AccountUpdateException("Error en el formato de los datos", e);
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

        // bank_client_id: puede estar en bank_details o credit_details
        long bdClientId = rs.getLong("bd_client_id");
        boolean bdClientNull = rs.wasNull();
        long cdClientId = rs.getLong("cd_client_id");
        boolean cdClientNull = rs.wasNull();

        if (!cdClientNull && cdClientId != 0) {
            account.setBankClientId(cdClientId);
        } else if (!bdClientNull && bdClientId != 0) {
            account.setBankClientId(bdClientId);
        }

        account.setAccountNumber(rs.getString("account_number"));
        account.setClabe(rs.getString("clabe"));

        // bank_details
        boolean transfer = rs.getBoolean("can_transfer_out");
        if (!rs.wasNull()) account.setCanTransferOut(transfer);

        // credit_details
        double creditLimit = rs.getDouble("credit_limit");
        if (!rs.wasNull()) account.setCreditLimit(creditLimit);
        double creditUsed = rs.getDouble("credit_used");
        if (!rs.wasNull()) account.setCreditUsed(creditUsed);
        int cutoff = rs.getInt("cutoff_day");
        if (!rs.wasNull()) account.setCutoffDay(cutoff);
        int payment = rs.getInt("payment_deadline_day");
        if (!rs.wasNull()) account.setPaymentDay(payment);

        // savings_details
        double annualYield = rs.getDouble("annual_yield");
        if (!rs.wasNull()) account.setAnnualYield(annualYield);
        double yieldCap = rs.getDouble("yield_cap_amount");
        if (!rs.wasNull()) account.setYieldCapAmount(yieldCap);
        java.sql.Date lastCalc = rs.getDate("last_yield_calculation");
        if (lastCalc != null) account.setLastYieldCalculation(lastCalc.toString());

        // investment_details
        String instrType = rs.getString("instrument_type");
        if (instrType != null) {
            account.setInstrumentType(instrType);
            int termDays = rs.getInt("term_days");
            if (!rs.wasNull()) account.setTermDays(termDays);
            double principal = rs.getDouble("principal_amount");
            if (!rs.wasNull()) account.setPrincipalAmount(principal);
            double invYield = rs.getDouble("inv_annual_yield");
            if (!rs.wasNull()) account.setInvestmentAnnualYield(invYield);
            int basis = rs.getInt("day_count_basis");
            if (!rs.wasNull()) account.setDayCountBasis(basis);
            java.sql.Date startDate = rs.getDate("start_date");
            if (startDate != null) account.setStartDate(startDate.toString());
            java.sql.Date maturityDate = rs.getDate("maturity_date");
            if (maturityDate != null) account.setMaturityDate(maturityDate.toString());
            account.setInvestmentStatus(rs.getString("inv_status"));
            boolean autoReinvest = rs.getBoolean("auto_reinvest");
            if (!rs.wasNull()) account.setAutoReinvest(autoReinvest);
            int reinvestTerm = rs.getInt("reinvest_term_days");
            if (!rs.wasNull()) account.setReinvestTermDays(reinvestTerm);
            double reinvestYield = rs.getDouble("reinvest_annual_yield");
            if (!rs.wasNull()) account.setReinvestAnnualYield(reinvestYield);
        }

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) account.setCreatedAt(ZonedDateTime.of(createdTs.toLocalDateTime(), java.time.ZoneId.systemDefault()));

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) account.setUpdatedAt(ZonedDateTime.of(updatedTs.toLocalDateTime(), java.time.ZoneId.systemDefault()));

        return account;
    }
}
