package com.giozar04.transactions.infrastructure.transport.socket;

import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.transactions.application.ports.input.TransactionOperations;
import com.giozar04.transactions.infrastructure.transport.socket.TransactionControllers;

public class TransactionHandlers implements ServerRegisterHandlers {

    private final TransactionOperations transactionService;

    public TransactionHandlers(TransactionOperations transactionService) {
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
        server.registerHandler(
            TransactionControllers.TransactionMessageTypes.GET_TRANSACTIONS_BY_USER,
            TransactionControllers.getTransactionsByUserController(transactionService)
        );
    }
}
