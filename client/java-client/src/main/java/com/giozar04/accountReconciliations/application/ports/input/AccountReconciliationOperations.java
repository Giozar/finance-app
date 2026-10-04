package com.giozar04.accountReconciliations.application.ports.input;

import java.util.List;
import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface AccountReconciliationOperations {
    List<AccountReconciliation> getAllAccountReconciliations() throws ClientOperationException;
    List<AccountReconciliation> getAccountReconciliationsByUserId(long userId) throws ClientOperationException;
    AccountReconciliation getAccountReconciliationByAccountId(long accountId) throws ClientOperationException;
    AccountReconciliation reconcileAccount(long accountId) throws ClientOperationException;
}
