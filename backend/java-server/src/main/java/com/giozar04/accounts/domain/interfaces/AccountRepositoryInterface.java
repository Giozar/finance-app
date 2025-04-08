package com.giozar04.accounts.domain.interfaces;

import java.util.List;

import com.giozar04.accounts.domain.entities.Account;

public interface AccountRepositoryInterface {
    Account createAccount(Account account);
    Account getAccountById(long id);
    Account updateAccountById(long id, Account account);
    void deleteAccountById(long id);
    List<Account> getAllAccounts();
}
