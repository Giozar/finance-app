package com.giozar04.accountReconciliations.application.usecases;

import java.util.List;
import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.accountReconciliations.application.ports.input.AccountReconciliationOperations;
import com.giozar04.accountReconciliations.application.ports.output.AccountReconciliationGateway;

public final class AccountReconciliationUseCase implements AccountReconciliationOperations {
    private final AccountReconciliationGateway gateway;

    public AccountReconciliationUseCase(AccountReconciliationGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public List<AccountReconciliation> getAllAccountReconciliations() throws ClientOperationException {
        return gateway.getAllAccountReconciliations();
    }

    @Override
    public List<AccountReconciliation> getAccountReconciliationsByUserId(long userId) throws ClientOperationException {
        return gateway.getAccountReconciliationsByUserId(userId);
    }

    @Override
    public AccountReconciliation getAccountReconciliationByAccountId(long accountId) throws ClientOperationException {
        return gateway.getAccountReconciliationByAccountId(accountId);
    }

    @Override
    public AccountReconciliation reconcileAccount(long accountId) throws ClientOperationException {
        return gateway.reconcileAccount(accountId);
    }
}
