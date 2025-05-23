package com.giozar04.cardTransactionDetails.infrastructure.handlers;

import com.giozar04.cardTransactionDetails.application.services.CardTransactionDetailService;
import com.giozar04.cardTransactionDetails.infrastructure.controllers.CardTransactionDetailControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class CardTransactionDetailHandlers implements ServerRegisterHandlers {

    private final CardTransactionDetailService service;

    public CardTransactionDetailHandlers(CardTransactionDetailService service) {
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
