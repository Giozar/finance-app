package com.giozar04.accountCashbackSettings.application.ports.input;

import java.util.List;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;

public interface AccountCashbackSettingOperations {
    AccountCashbackSetting createAccountCashbackSetting(AccountCashbackSetting setting);
    AccountCashbackSetting getAccountCashbackSettingByAccountId(long accountId);
    AccountCashbackSetting updateAccountCashbackSettingByAccountId(long accountId, AccountCashbackSetting setting);
    void deleteAccountCashbackSettingByAccountId(long accountId);
    List<AccountCashbackSetting> getAllAccountCashbackSettings();
}
