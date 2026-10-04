package com.giozar04.accountCashbackSettings.application.usecases;

import java.util.List;

import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.accountCashbackSettings.application.ports.output.AccountCashbackSettingRepository;
import com.giozar04.accountCashbackSettings.application.ports.input.AccountCashbackSettingOperations;

public class AccountCashbackSettingUseCase implements AccountCashbackSettingOperations {

    private final AccountCashbackSettingRepository repository;

    public AccountCashbackSettingUseCase(AccountCashbackSettingRepository repository) {
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
