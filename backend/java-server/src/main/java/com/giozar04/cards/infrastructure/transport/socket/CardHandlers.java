package com.giozar04.cards.infrastructure.transport.socket;

import com.giozar04.cards.application.ports.input.CardOperations;
import com.giozar04.cards.infrastructure.transport.socket.CardControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class CardHandlers implements ServerRegisterHandlers {

    private final CardOperations cardService;

    public CardHandlers(CardOperations cardService) {
        this.cardService = cardService;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            CardControllers.CardMessageTypes.CREATE_CARD,
            CardControllers.createCardController(cardService)
        );
        server.registerHandler(
            CardControllers.CardMessageTypes.GET_CARD,
            CardControllers.getCardController(cardService)
        );
        server.registerHandler(
            CardControllers.CardMessageTypes.UPDATE_CARD,
            CardControllers.updateCardController(cardService)
        );
        server.registerHandler(
            CardControllers.CardMessageTypes.DELETE_CARD,
            CardControllers.deleteCardController(cardService)
        );
        server.registerHandler(
            CardControllers.CardMessageTypes.GET_ALL_CARDS,
            CardControllers.getAllCardsController(cardService)
        );
        server.registerHandler(
            CardControllers.CardMessageTypes.GET_CARDS_BY_ACCOUNT,
            CardControllers.getCardsByAccountController(cardService)
        );
    }
}
