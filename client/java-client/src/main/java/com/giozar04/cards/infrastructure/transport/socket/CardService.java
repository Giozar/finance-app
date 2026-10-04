package com.giozar04.cards.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.card.infrastructure.serialization.CardMapper;
import com.giozar04.card.domain.entities.Card;
import com.giozar04.card.application.exceptions.CardCreationException;
import com.giozar04.card.application.exceptions.CardDeletionException;
import com.giozar04.card.infrastructure.serialization.CardParsingException;
import com.giozar04.card.application.exceptions.CardRetrievalException;
import com.giozar04.card.application.exceptions.CardUpdateException;
import com.giozar04.cards.application.ports.output.CardGateway;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.infrastructure.transport.socket.ServerConnectionService;
import com.giozar04.serverConnection.infrastructure.transport.socket.ServerResponseValidator;

public class CardService implements CardGateway {

    private final ServerConnectionService serverConnectionService;
    private static final ConsoleLogger logger = ConsoleLogger.getInstance();
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
        message.addData("card", CardMapper.toMap(card));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_CARD");
            ServerResponseValidator.validateResponse(response);
            logger.info("Tarjeta creada exitosamente: " + response);
            return CardMapper.fromMap((Map<String, Object>) response.getData("card"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Card updateCardById(Long id, Card card) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_CARD");
        message.addData("id", id);
        message.addData("card", CardMapper.toMap(card));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_CARD");
            ServerResponseValidator.validateResponse(response);
            logger.info("Tarjeta actualizada correctamente: " + response);
            return CardMapper.fromMap((Map<String, Object>) response.getData("card"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardUpdateException("Error al esperar respuesta del servidor", e);
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
            throw new CardDeletionException("Error al esperar respuesta del servidor", e);
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
                throw new CardRetrievalException("Lista de tarjetas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Card> cards = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        cards.add(CardMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Tarjetas obtenidas correctamente. Total: " + cards.size());
                return cards;
            } else {
                throw new CardParsingException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Card> getCardsByAccountId(long accountId) throws ClientOperationException {
        logger.info("Solicitando tarjetas de la cuenta " + accountId + "...");
        Message message = new Message();
        message.setType("GET_CARDS_BY_ACCOUNT");
        message.addData("accountId", accountId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_CARDS_BY_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("cards");

            if (raw == null) {
                throw new CardRetrievalException("Lista de tarjetas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Card> cards = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        cards.add(CardMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Tarjetas de la cuenta obtenidas correctamente. Total: " + cards.size());
                return cards;
            } else {
                throw new CardParsingException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
