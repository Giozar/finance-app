package com.giozar04.accountReconciliations.application.ports.output;

import java.util.List;

import com.giozar04.accountReconciliations.domain.entities.AccountReconciliation;

public interface AccountReconciliationRepository {

    List<AccountReconciliation> getAllAccountReconciliations();

    List<AccountReconciliation> getAccountReconciliationsByUserId(long userId);

    AccountReconciliation getAccountReconciliationByAccountId(long accountId);

    // Ejecuta sp_reconcile_account y devuelve la fila actualizada de la vista
    AccountReconciliation reconcileAccount(long accountId);
}
