package com.giozar04.accounts.application.ports.input;

import java.util.List;

import com.giozar04.accounts.domain.entities.Account;

public interface AccountOperations {
    Account createAccount(Account account);
    Account getAccountById(long id);
    Account updateAccountById(long id, Account account);
    void deleteAccountById(long id);
    List<Account> getAllAccounts();
    List<Account> getAccountsByUserId(long userId);
}
