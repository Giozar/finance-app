package com.giozar04.transactions.infrastructure.handlers;

import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.infrastructure.controllers.TransactionControllers;

public class TransactionHandlers implements ServerRegisterHandlers {
    private final TransactionService transactionService;

    public TransactionHandlers(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            TransactionControllers.TransactionMessageTypes.CREATE_TRANSACTION,
            TransactionControllers.createTransactionController(transactionService)
        );
        server.registerHandler(
            TransactionControllers.TransactionMessageTypes.GET_TRANSACTION,
            TransactionControllers.getTransactionController(transactionService)
        );
        server.registerHandler(
            TransactionControllers.TransactionMessageTypes.UPDATE_TRANSACTION,
            TransactionControllers.updateTransactionController(transactionService)
        );
        server.registerHandler(
            TransactionControllers.TransactionMessageTypes.DELETE_TRANSACTION,
            TransactionControllers.deleteTransactionController(transactionService)
        );
        server.registerHandler(
            TransactionControllers.TransactionMessageTypes.GET_ALL_TRANSACTIONS,
            TransactionControllers.getAllTransactionsController(transactionService)
        );
    }
}
