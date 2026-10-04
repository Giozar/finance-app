package com.giozar04.accountReconciliations.application.usecases;

import java.util.List;

import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.accountReconciliations.application.ports.output.AccountReconciliationRepository;
import com.giozar04.accountReconciliations.application.ports.input.AccountReconciliationOperations;

public class AccountReconciliationUseCase implements AccountReconciliationOperations {

    private final AccountReconciliationRepository accountReconciliationRepository;

    public AccountReconciliationUseCase(AccountReconciliationRepository accountReconciliationRepository) {
        this.accountReconciliationRepository = accountReconciliationRepository;
    }

    @Override
    public List<AccountReconciliation> getAllAccountReconciliations() {
        return accountReconciliationRepository.getAllAccountReconciliations();
    }

    @Override
    public List<AccountReconciliation> getAccountReconciliationsByUserId(long userId) {
        return accountReconciliationRepository.getAccountReconciliationsByUserId(userId);
    }

    @Override
    public AccountReconciliation getAccountReconciliationByAccountId(long accountId) {
        return accountReconciliationRepository.getAccountReconciliationByAccountId(accountId);
    }

    @Override
    public AccountReconciliation reconcileAccount(long accountId) {
        return accountReconciliationRepository.reconcileAccount(accountId);
    }
}
