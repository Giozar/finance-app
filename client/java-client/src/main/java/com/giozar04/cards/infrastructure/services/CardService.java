package com.giozar04.cards.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.card.application.utils.CardUtils;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.domain.exceptions.CardExceptions;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;

public class CardService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static CardService instance;

    private CardService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static CardService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new CardService(serverConnectionService);
        }
        return instance;
    }

    public static CardService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public Card createCard(Card card) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_CARD");
        message.addData("card", CardUtils.cardToMap(card));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_CARD");
            ServerResponseValidator.validateResponse(response);
            logger.info("Tarjeta creada exitosamente: " + response);
            return CardUtils.mapToCard((Map<String, Object>) response.getData("card"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardExceptions.CardCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Card updateCardById(Long id, Card card) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_CARD");
        message.addData("id", id);
        message.addData("card", CardUtils.cardToMap(card));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_CARD");
            ServerResponseValidator.validateResponse(response);
            logger.info("Tarjeta actualizada correctamente: " + response);
            return CardUtils.mapToCard((Map<String, Object>) response.getData("card"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardExceptions.CardUpdateException("Error al esperar respuesta del servidor", e);
        }
    }

    public void deleteCardById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_CARD");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_CARD");
            ServerResponseValidator.validateResponse(response);
            logger.info("Tarjeta eliminada exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardExceptions.CardDeletionException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Card> getAllCards() throws ClientOperationException {
        logger.info("Solicitando todas las tarjetas...");
        Message message = new Message();
        message.setType("GET_ALL_CARDS");

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_CARDS");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("cards");

            if (raw == null) {
                throw new CardExceptions.CardRetrievalException("Lista de tarjetas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Card> cards = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        cards.add(CardUtils.mapToCard((Map<String, Object>) map));
                    }
                }
                logger.info("Tarjetas obtenidas correctamente. Total: " + cards.size());
                return cards;
            } else {
                throw new CardExceptions.CardParsingException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardExceptions.CardRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
