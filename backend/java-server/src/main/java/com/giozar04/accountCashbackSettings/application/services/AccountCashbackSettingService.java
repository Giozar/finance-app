package com.giozar04.accountCashbackSettings.application.services;

import java.util.List;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.accountCashbackSettings.domain.interfaces.AccountCashbackSettingRepositoryInterface;

public class AccountCashbackSettingService implements AccountCashbackSettingRepositoryInterface {

    private final AccountCashbackSettingRepositoryInterface repository;

    public AccountCashbackSettingService(AccountCashbackSettingRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public AccountCashbackSetting createAccountCashbackSetting(AccountCashbackSetting setting) {
        return repository.createAccountCashbackSetting(setting);
    }

    @Override
    public AccountCashbackSetting getAccountCashbackSettingByAccountId(long accountId) {
        return repository.getAccountCashbackSettingByAccountId(accountId);
    }

    @Override
    public AccountCashbackSetting updateAccountCashbackSettingByAccountId(long accountId, AccountCashbackSetting setting) {
        return repository.updateAccountCashbackSettingByAccountId(accountId, setting);
    }

    @Override
    public void deleteAccountCashbackSettingByAccountId(long accountId) {
        repository.deleteAccountCashbackSettingByAccountId(accountId);
    }

    @Override
    public List<AccountCashbackSetting> getAllAccountCashbackSettings() {
        return repository.getAllAccountCashbackSettings();
    }
}
