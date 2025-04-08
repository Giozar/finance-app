package com.giozar04.accounts.infrastructure.handlers;

import com.giozar04.accounts.application.services.AccountService;
import com.giozar04.accounts.infrastructure.controllers.AccountControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class AccountHandlers implements ServerRegisterHandlers {

    private final AccountService accountService;

    public AccountHandlers(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            AccountControllers.AccountMessageTypes.CREATE_ACCOUNT,
            AccountControllers.createAccountController(accountService)
        );
        server.registerHandler(
            AccountControllers.AccountMessageTypes.GET_ACCOUNT,
            AccountControllers.getAccountController(accountService)
        );
        server.registerHandler(
            AccountControllers.AccountMessageTypes.UPDATE_ACCOUNT,
            AccountControllers.updateAccountController(accountService)
        );
        server.registerHandler(
            AccountControllers.AccountMessageTypes.DELETE_ACCOUNT,
            AccountControllers.deleteAccountController(accountService)
        );
        server.registerHandler(
            AccountControllers.AccountMessageTypes.GET_ALL_ACCOUNTS,
            AccountControllers.getAllAccountsController(accountService)
        );
    }
}
