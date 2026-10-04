package com.giozar04.accountCashbackSettings.infrastructure.persistence.mysql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.accountCashbackSettings.application.ports.output.AccountCashbackSettingRepository;
import com.giozar04.accountCashbackSettings.domain.policies.AccountCashbackSettingPolicy;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;

public abstract class AbstractAccountCashbackSettingJdbcRepository implements AccountCashbackSettingRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractAccountCashbackSettingJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection,
                "La conexión a base de datos no puede ser nula");
    }

    protected void validateSetting(AccountCashbackSetting setting) {
        AccountCashbackSettingPolicy.validateSetting(setting);
    }

    protected void validateAccountId(long accountId) {
        AccountCashbackSettingPolicy.validateAccountId(accountId);
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
