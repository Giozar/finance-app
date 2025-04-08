package com.giozar04.accounts.application.services;

import java.util.List;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.interfaces.AccountRepositoryInterface;

public class AccountService implements AccountRepositoryInterface {

    private final AccountRepositoryInterface accountRepository;

    public AccountService(AccountRepositoryInterface accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(Account account) {
        return accountRepository.createAccount(account);
    }

    @Override
    public Account getAccountById(long id) {
        return accountRepository.getAccountById(id);
    }

    @Override
    public Account updateAccountById(long id, Account account) {
        return accountRepository.updateAccountById(id, account);
    }

    @Override
    public void deleteAccountById(long id) {
        accountRepository.deleteAccountById(id);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.getAllAccounts();
    }
}
