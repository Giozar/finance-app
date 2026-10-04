package com.giozar04.cardTransactionDetails.infrastructure.transport.socket;

import com.giozar04.cardTransactionDetails.application.ports.input.CardTransactionDetailOperations;
import com.giozar04.cardTransactionDetails.infrastructure.transport.socket.CardTransactionDetailControllers;
import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;

public class CardTransactionDetailHandlers implements ServerRegisterHandlers {

    private final CardTransactionDetailOperations service;

    public CardTransactionDetailHandlers(CardTransactionDetailOperations service) {
        this.service = service;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            CardTransactionDetailControllers.MessageTypes.CREATE_DETAIL,
            CardTransactionDetailControllers.createDetailController(service)
        );
        server.registerHandler(
            CardTransactionDetailControllers.MessageTypes.GET_DETAIL,
            CardTransactionDetailControllers.getDetailController(service)
        );
        server.registerHandler(
            CardTransactionDetailControllers.MessageTypes.UPDATE_DETAIL,
            CardTransactionDetailControllers.updateDetailController(service)
        );
        server.registerHandler(
            CardTransactionDetailControllers.MessageTypes.DELETE_DETAIL,
            CardTransactionDetailControllers.deleteDetailController(service)
        );
        server.registerHandler(
            CardTransactionDetailControllers.MessageTypes.GET_ALL_DETAILS,
            CardTransactionDetailControllers.getAllDetailsController(service)
        );
        server.registerHandler(
            CardTransactionDetailControllers.MessageTypes.GET_DETAILS_BY_TRANSACTION,
            CardTransactionDetailControllers.getDetailsByTransactionController(service)
        );
    }
}
