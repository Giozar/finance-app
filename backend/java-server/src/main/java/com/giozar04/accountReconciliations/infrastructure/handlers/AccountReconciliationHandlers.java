package com.giozar04.accountReconciliations.infrastructure.handlers;

import com.giozar04.accountReconciliations.application.services.AccountReconciliationService;
import com.giozar04.accountReconciliations.infrastructure.controllers.AccountReconciliationControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class AccountReconciliationHandlers implements ServerRegisterHandlers {

    private final AccountReconciliationService accountReconciliationService;

    public AccountReconciliationHandlers(AccountReconciliationService accountReconciliationService) {
        this.accountReconciliationService = accountReconciliationService;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            AccountReconciliationControllers.AccountReconciliationMessageTypes.GET_ALL_ACCOUNT_RECONCILIATIONS,
            AccountReconciliationControllers.getAllAccountReconciliationsController(accountReconciliationService)
        );
        server.registerHandler(
            AccountReconciliationControllers.AccountReconciliationMessageTypes.GET_ACCOUNT_RECONCILIATIONS_BY_USER,
            AccountReconciliationControllers.getAccountReconciliationsByUserController(accountReconciliationService)
        );
        server.registerHandler(
            AccountReconciliationControllers.AccountReconciliationMessageTypes.GET_ACCOUNT_RECONCILIATION,
            AccountReconciliationControllers.getAccountReconciliationController(accountReconciliationService)
        );
        server.registerHandler(
            AccountReconciliationControllers.AccountReconciliationMessageTypes.RECONCILE_ACCOUNT,
            AccountReconciliationControllers.reconcileAccountController(accountReconciliationService)
        );
    }
}
