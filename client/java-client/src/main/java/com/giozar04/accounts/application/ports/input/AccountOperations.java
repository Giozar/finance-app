package com.giozar04.accounts.application.ports.input;

import java.util.List;
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface AccountOperations {
    Account createAccount(Account account) throws ClientOperationException;
    Account updateAccountById(Long id, Account account) throws ClientOperationException;
    void deleteAccountById(Long id) throws ClientOperationException;
    Account getAccountById(Long id) throws ClientOperationException;
    List<Account> getAllAccounts() throws ClientOperationException;
    List<Account> getAccountsByUserId(long userId) throws ClientOperationException;
}
