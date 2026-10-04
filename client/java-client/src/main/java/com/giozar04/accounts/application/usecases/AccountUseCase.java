package com.giozar04.accounts.application.usecases;

import java.util.List;
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.accounts.application.ports.input.AccountOperations;
import com.giozar04.accounts.application.ports.output.AccountGateway;

public final class AccountUseCase implements AccountOperations {
    private final AccountGateway gateway;

    public AccountUseCase(AccountGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public Account createAccount(Account account) throws ClientOperationException {
        return gateway.createAccount(account);
    }

    @Override
    public Account updateAccountById(Long id, Account account) throws ClientOperationException {
        return gateway.updateAccountById(id, account);
    }

    @Override
    public void deleteAccountById(Long id) throws ClientOperationException {
        gateway.deleteAccountById(id);
    }

    @Override
    public Account getAccountById(Long id) throws ClientOperationException {
        return gateway.getAccountById(id);
    }

    @Override
    public List<Account> getAllAccounts() throws ClientOperationException {
        return gateway.getAllAccounts();
    }

    @Override
    public List<Account> getAccountsByUserId(long userId) throws ClientOperationException {
        return gateway.getAccountsByUserId(userId);
    }
}
