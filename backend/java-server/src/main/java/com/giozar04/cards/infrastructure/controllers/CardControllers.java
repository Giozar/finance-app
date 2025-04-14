package com.giozar04.cards.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.card.application.utils.CardUtils;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.cards.application.services.CardService;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;

public class CardControllers {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public static final class CardMessageTypes {
        public static final String CREATE_CARD = "CREATE_CARD";
        public static final String GET_CARD = "GET_CARD";
        public static final String UPDATE_CARD = "UPDATE_CARD";
        public static final String DELETE_CARD = "DELETE_CARD";
        public static final String GET_ALL_CARDS = "GET_ALL_CARDS";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createCardController(CardService cardService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de creación de tarjeta");

            Map<String, Object> data = (Map<String, Object>) message.getData("card");
            if (data == null) {
                return Message.createErrorMessage(CardMessageTypes.CREATE_CARD, "Datos de tarjeta no proporcionados");
            }

            Card card = CardUtils.mapToCard(data);
            Card created = cardService.createCard(card);

            Message response = Message.createSuccessMessage(CardMessageTypes.CREATE_CARD, "Tarjeta creada exitosamente");
            response.addData("card", CardUtils.cardToMap(created));
            return response;
        };
    }

    public static MessageHandler getCardController(CardService cardService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de tarjeta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(CardMessageTypes.GET_CARD, "ID de tarjeta inválido o no proporcionado");
            }

            Card card = cardService.getCardById(id);
            Message response = Message.createSuccessMessage(CardMessageTypes.GET_CARD, "Tarjeta obtenida exitosamente");
            response.addData("card", CardUtils.cardToMap(card));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateCardController(CardService cardService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de actualización de tarjeta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(CardMessageTypes.UPDATE_CARD, "ID de tarjeta inválido o no proporcionado");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("card");
            if (data == null) {
                return Message.createErrorMessage(CardMessageTypes.UPDATE_CARD, "Datos de tarjeta no proporcionados");
            }

            Card card = CardUtils.mapToCard(data);
            Card updated = cardService.updateCardById(id, card);

            Message response = Message.createSuccessMessage(CardMessageTypes.UPDATE_CARD, "Tarjeta actualizada exitosamente");
            response.addData("card", CardUtils.cardToMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteCardController(CardService cardService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de eliminación de tarjeta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(CardMessageTypes.DELETE_CARD, "ID de tarjeta inválido o no proporcionado");
            }

            cardService.deleteCardById(id);
            return Message.createSuccessMessage(CardMessageTypes.DELETE_CARD, "Tarjeta eliminada exitosamente");
        };
    }

    public static MessageHandler getAllCardsController(CardService cardService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de todas las tarjetas");

            List<Card> cards = cardService.getAllCards();
            List<Map<String, Object>> list = new ArrayList<>();

            for (Card card : cards) {
                list.add(CardUtils.cardToMap(card));
            }

            Message response = Message.createSuccessMessage(CardMessageTypes.GET_ALL_CARDS, "Tarjetas obtenidas exitosamente");
            response.addData("cards", list);
            response.addData("count", list.size());

            return response;
        };
    }

    private static Long parseId(Object rawId) {
        if (rawId instanceof Long l) return l;
        if (rawId instanceof String s) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
