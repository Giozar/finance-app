package com.giozar04.cards.infrastructure.handlers;

import com.giozar04.cards.application.services.CardService;
import com.giozar04.cards.infrastructure.controllers.CardControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class CardHandlers implements ServerRegisterHandlers {

    private final CardService cardService;

    public CardHandlers(CardService cardService) {
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
    }
}
