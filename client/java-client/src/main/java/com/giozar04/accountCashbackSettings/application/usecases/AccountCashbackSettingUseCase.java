package com.giozar04.accountCashbackSettings.application.usecases;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.accountCashbackSettings.application.ports.input.AccountCashbackSettingOperations;
import com.giozar04.accountCashbackSettings.application.ports.output.AccountCashbackSettingGateway;

public final class AccountCashbackSettingUseCase implements AccountCashbackSettingOperations {
    private final AccountCashbackSettingGateway gateway;

    public AccountCashbackSettingUseCase(AccountCashbackSettingGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public AccountCashbackSetting createAccountCashbackSetting(AccountCashbackSetting setting) throws ClientOperationException {
        return gateway.createAccountCashbackSetting(setting);
    }

    @Override
    public AccountCashbackSetting getAccountCashbackSettingByAccountId(Long accountId) throws ClientOperationException {
        return gateway.getAccountCashbackSettingByAccountId(accountId);
    }

    @Override
    public AccountCashbackSetting updateAccountCashbackSettingByAccountId(Long accountId, AccountCashbackSetting setting) throws ClientOperationException {
        return gateway.updateAccountCashbackSettingByAccountId(accountId, setting);
    }

    @Override
    public void deleteAccountCashbackSettingByAccountId(Long accountId) throws ClientOperationException {
        gateway.deleteAccountCashbackSettingByAccountId(accountId);
    }
}
