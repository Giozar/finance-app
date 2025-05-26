package com.giozar04.transactions.infrastructure.handlers;

import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.infrastructure.controllers.TransactionControllers;

public class TransactionHandlers implements ServerRegisterHandlers {

    private final TransactionService service;

    public TransactionHandlers(TransactionService service) {
        this.service = service;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(TransactionControllers.MessageTypes.CREATE, TransactionControllers.createTransactionController(service));
        server.registerHandler(TransactionControllers.MessageTypes.GET, TransactionControllers.getTransactionController(service));
        server.registerHandler(TransactionControllers.MessageTypes.UPDATE, TransactionControllers.updateTransactionController(service));
        server.registerHandler(TransactionControllers.MessageTypes.DELETE, TransactionControllers.deleteTransactionController(service));
        server.registerHandler(TransactionControllers.MessageTypes.GET_ALL, TransactionControllers.getAllTransactionsController(service));
    }
}
