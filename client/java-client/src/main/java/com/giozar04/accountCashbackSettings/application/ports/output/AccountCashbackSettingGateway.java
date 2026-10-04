package com.giozar04.accountCashbackSettings.application.ports.output;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface AccountCashbackSettingGateway {
    AccountCashbackSetting createAccountCashbackSetting(AccountCashbackSetting setting) throws ClientOperationException;
    AccountCashbackSetting getAccountCashbackSettingByAccountId(Long accountId) throws ClientOperationException;
    AccountCashbackSetting updateAccountCashbackSettingByAccountId(Long accountId, AccountCashbackSetting setting) throws ClientOperationException;
    void deleteAccountCashbackSettingByAccountId(Long accountId) throws ClientOperationException;
}
