package com.giozar04.accountReconciliations.infrastructure.repositories;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.accountReconciliations.application.exceptions.AccountReconciliationAdjustmentException;
import com.giozar04.accountReconciliations.application.exceptions.AccountReconciliationRetrievalException;
import com.giozar04.accountReconciliations.domain.models.AccountReconciliationRepositoryAbstract;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;

// Solo lectura sobre la vista v_account_reconciliation; la escritura la hace sp_reconcile_account
public class AccountReconciliationRepositoryMySQL extends AccountReconciliationRepositoryAbstract {

    private static final String SQL_SELECT_BASE = """
        SELECT account_id, user_id, account_name, account_type,
               opening_net, total_inflows, total_outflows, expected_net, actual_net, difference
        FROM v_account_reconciliation
    """;

    private static final String SQL_SELECT_ALL = SQL_SELECT_BASE + " ORDER BY account_id";
    private static final String SQL_SELECT_BY_USER = SQL_SELECT_BASE + " WHERE user_id = ? ORDER BY account_id";
    private static final String SQL_SELECT_BY_ACCOUNT = SQL_SELECT_BASE + " WHERE account_id = ?";

    private static final String SQL_CALL_RECONCILE = "{CALL sp_reconcile_account(?)}";

    public AccountReconciliationRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<AccountReconciliation> getAllAccountReconciliations() {
        List<AccountReconciliation> reconciliations = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reconciliations.add(mapResultSetToAccountReconciliation(rs));
            }
            return reconciliations;

        } catch (SQLException e) {
            throw new AccountReconciliationRetrievalException(
                "Error al obtener las reconciliaciones de cuentas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AccountReconciliation> getAccountReconciliationsByUserId(long userId) {
        validateId(userId);
        List<AccountReconciliation> reconciliations = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_USER)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reconciliations.add(mapResultSetToAccountReconciliation(rs));
                }
            }
            return reconciliations;

        } catch (SQLException e) {
            throw new AccountReconciliationRetrievalException(
                "Error al obtener las reconciliaciones del usuario con ID " + userId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public AccountReconciliation getAccountReconciliationByAccountId(long accountId) {
        validateId(accountId);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ACCOUNT)) {

            stmt.setLong(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccountReconciliation(rs);
                }
                throw new AccountReconciliationRetrievalException(
                    "Cuenta no encontrada con ID: " + accountId);
            }

        } catch (SQLException e) {
            throw new AccountReconciliationRetrievalException(
                "Error al obtener la reconciliación de la cuenta con ID " + accountId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public AccountReconciliation reconcileAccount(long accountId) {
        validateId(accountId);

        try (Connection conn = databaseConnection.getConnection()) {

            // 1. Ajustar la cuenta al saldo esperado (puede lanzar SIGNAL con mensaje en español)
            try (CallableStatement stmt = conn.prepareCall(SQL_CALL_RECONCILE)) {
                stmt.setLong(1, accountId);
                stmt.execute();
            }

            databaseConnection.commitTransaction();
            logger.info("Cuenta reconciliada con ID: " + accountId);

            // 2. Devolver la fila actualizada de la vista
            try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ACCOUNT)) {
                stmt.setLong(1, accountId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAccountReconciliation(rs);
                    }
                    throw new AccountReconciliationAdjustmentException(
                        "Cuenta no encontrada con ID: " + accountId);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al reconciliar la cuenta con ID " + accountId + ": " + e.getMessage(), e);
            rollback();
            throw new AccountReconciliationAdjustmentException(
                "Error al reconciliar la cuenta: " + e.getMessage(), e);
        }
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException ex) {
            logger.error("Error al hacer rollback: " + ex.getMessage());
        }
    }

    private AccountReconciliation mapResultSetToAccountReconciliation(ResultSet rs) throws SQLException {
        AccountReconciliation reconciliation = new AccountReconciliation();
        reconciliation.setAccountId(rs.getLong("account_id"));
        reconciliation.setUserId(rs.getLong("user_id"));
        reconciliation.setAccountName(rs.getString("account_name"));

        String type = rs.getString("account_type");
        if (type != null) reconciliation.setAccountType(AccountTypes.fromValue(type));

        reconciliation.setOpeningNet(rs.getBigDecimal("opening_net"));
        reconciliation.setTotalInflows(rs.getBigDecimal("total_inflows"));
        reconciliation.setTotalOutflows(rs.getBigDecimal("total_outflows"));
        reconciliation.setExpectedNet(rs.getBigDecimal("expected_net"));
        reconciliation.setActualNet(rs.getBigDecimal("actual_net"));
        reconciliation.setDifference(rs.getBigDecimal("difference"));
        return reconciliation;
    }
}
