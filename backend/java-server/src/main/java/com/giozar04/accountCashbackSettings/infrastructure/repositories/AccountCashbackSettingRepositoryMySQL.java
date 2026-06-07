package com.giozar04.accountCashbackSettings.infrastructure.repositories;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.accountCashbackSettings.domain.exceptions.AccountCashbackSettingExceptions;
import com.giozar04.accountCashbackSettings.domain.models.AccountCashbackSettingRepositoryAbstract;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;

public class AccountCashbackSettingRepositoryMySQL extends AccountCashbackSettingRepositoryAbstract {

    private static final String SQL_INSERT = """
        INSERT INTO account_cashback_settings (account_id, cashback_enabled, default_cashback_rate, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?)
    """;

    private static final String SQL_SELECT_BY_ACCOUNT_ID =
        "SELECT * FROM account_cashback_settings WHERE account_id = ?";

    private static final String SQL_UPDATE = """
        UPDATE account_cashback_settings
        SET cashback_enabled = ?, default_cashback_rate = ?, updated_at = ?
        WHERE account_id = ?
    """;

    private static final String SQL_DELETE =
        "DELETE FROM account_cashback_settings WHERE account_id = ?";

    private static final String SQL_SELECT_ALL =
        "SELECT * FROM account_cashback_settings";

    public AccountCashbackSettingRepositoryMySQL(DatabaseConnectionInterface databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public AccountCashbackSetting createAccountCashbackSetting(AccountCashbackSetting setting) {
        validateSetting(setting);

        if (setting.getCreatedAt() == null) setting.setCreatedAt(ZonedDateTime.now());
        if (setting.getUpdatedAt() == null) setting.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            stmt.setLong(1, setting.getAccountId());
            stmt.setBoolean(2, setting.isCashbackEnabled());
            if (setting.getDefaultCashbackRate() != null) {
                stmt.setBigDecimal(3, setting.getDefaultCashbackRate());
            } else {
                stmt.setNull(3, java.sql.Types.DECIMAL);
            }
            stmt.setTimestamp(4, Timestamp.valueOf(setting.getCreatedAt().toLocalDateTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(setting.getUpdatedAt().toLocalDateTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("No se pudo insertar la configuración de cashback");
            }

            databaseConnection.commitTransaction();
            logger.info("Configuración de cashback creada para accountId: " + setting.getAccountId());
            return setting;

        } catch (SQLException e) {
            rollback();
            throw new AccountCashbackSettingExceptions.AccountCashbackSettingCreationException(
                "Error al crear la configuración de cashback para accountId: " + setting.getAccountId(), e);
        }
    }

    @Override
    public AccountCashbackSetting getAccountCashbackSettingByAccountId(long accountId) {
        validateAccountId(accountId);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ACCOUNT_ID)) {

            stmt.setLong(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                } else {
                    throw new AccountCashbackSettingExceptions.AccountCashbackSettingNotFoundException(
                        "No se encontró configuración de cashback para accountId: " + accountId, null);
                }
            }

        } catch (SQLException e) {
            throw new AccountCashbackSettingExceptions.AccountCashbackSettingRetrievalException(
                "Error al obtener la configuración de cashback para accountId: " + accountId, e);
        }
    }

    @Override
    public AccountCashbackSetting updateAccountCashbackSettingByAccountId(long accountId, AccountCashbackSetting setting) {
        validateAccountId(accountId);
        validateSetting(setting);
        setting.setUpdatedAt(ZonedDateTime.now());

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setBoolean(1, setting.isCashbackEnabled());
            if (setting.getDefaultCashbackRate() != null) {
                stmt.setBigDecimal(2, setting.getDefaultCashbackRate());
            } else {
                stmt.setNull(2, java.sql.Types.DECIMAL);
            }
            stmt.setTimestamp(3, Timestamp.valueOf(setting.getUpdatedAt().toLocalDateTime()));
            stmt.setLong(4, accountId);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new AccountCashbackSettingExceptions.AccountCashbackSettingNotFoundException(
                    "No se encontró configuración de cashback para actualizar, accountId: " + accountId, null);
            }

            databaseConnection.commitTransaction();
            setting.setAccountId(accountId);
            logger.info("Configuración de cashback actualizada para accountId: " + accountId);
            return setting;

        } catch (SQLException e) {
            rollback();
            throw new AccountCashbackSettingExceptions.AccountCashbackSettingUpdateException(
                "Error al actualizar la configuración de cashback para accountId: " + accountId, e);
        }
    }

    @Override
    public void deleteAccountCashbackSettingByAccountId(long accountId) {
        validateAccountId(accountId);

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, accountId);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new AccountCashbackSettingExceptions.AccountCashbackSettingNotFoundException(
                    "No se encontró configuración de cashback para eliminar, accountId: " + accountId, null);
            }

            databaseConnection.commitTransaction();
            logger.info("Configuración de cashback eliminada para accountId: " + accountId);

        } catch (SQLException e) {
            rollback();
            throw new AccountCashbackSettingExceptions.AccountCashbackSettingDeletionException(
                "Error al eliminar la configuración de cashback para accountId: " + accountId, e);
        }
    }

    @Override
    public List<AccountCashbackSetting> getAllAccountCashbackSettings() {
        List<AccountCashbackSetting> list = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new AccountCashbackSettingExceptions.AccountCashbackSettingRetrievalException(
                "Error al obtener todas las configuraciones de cashback", e);
        }
    }

    private AccountCashbackSetting mapResultSet(ResultSet rs) throws SQLException {
        AccountCashbackSetting setting = new AccountCashbackSetting();
        ZoneId zone = ZoneId.systemDefault();

        setting.setAccountId(rs.getLong("account_id"));
        setting.setCashbackEnabled(rs.getBoolean("cashback_enabled"));

        BigDecimal rate = rs.getBigDecimal("default_cashback_rate");
        setting.setDefaultCashbackRate(rate);

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            setting.setCreatedAt(ZonedDateTime.of(created.toLocalDateTime(), zone));
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            setting.setUpdatedAt(ZonedDateTime.of(updated.toLocalDateTime(), zone));
        }

        return setting;
    }

    private void rollback() {
        try {
            databaseConnection.rollbackTransaction();
        } catch (SQLException e) {
            logger.error("Error al hacer rollback de account_cashback_settings", e);
        }
    }
}
