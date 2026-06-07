package com.giozar04.accountCashbackSettings.domain.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.accountCashbackSettings.domain.interfaces.AccountCashbackSettingRepositoryInterface;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;

public abstract class AccountCashbackSettingRepositoryAbstract implements AccountCashbackSettingRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected AccountCashbackSettingRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection,
                "La conexión a base de datos no puede ser nula");
    }

    protected void validateSetting(AccountCashbackSetting setting) {
        Objects.requireNonNull(setting, "La configuración de cashback no puede ser nula");

        if (setting.getAccountId() <= 0) {
            throw new IllegalArgumentException("El accountId debe ser mayor que cero");
        }

        BigDecimal rate = setting.getDefaultCashbackRate();
        if (rate != null) {
            if (rate.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("defaultCashbackRate no puede ser negativo");
            }
            if (rate.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("defaultCashbackRate no puede ser mayor a 1 (100%)");
            }
        }
    }

    protected void validateAccountId(long accountId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("El accountId debe ser mayor que cero");
        }
    }

    @Override
    public abstract AccountCashbackSetting createAccountCashbackSetting(AccountCashbackSetting setting);

    @Override
    public abstract AccountCashbackSetting getAccountCashbackSettingByAccountId(long accountId);

    @Override
    public abstract AccountCashbackSetting updateAccountCashbackSettingByAccountId(long accountId, AccountCashbackSetting setting);

    @Override
    public abstract void deleteAccountCashbackSettingByAccountId(long accountId);

    @Override
    public abstract List<AccountCashbackSetting> getAllAccountCashbackSettings();
}
