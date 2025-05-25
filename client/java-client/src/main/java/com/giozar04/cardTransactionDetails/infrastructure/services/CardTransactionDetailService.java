package com.giozar04.cardTransactionDetails.infrastructure.services;

import java.util.Map;

import com.giozar04.cardTransactionDetails.application.utils.CardTransactionDetailUtils;
import com.giozar04.cardTransactionDetails.domain.entities.CardTransactionDetail;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;

public class CardTransactionDetailService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static CardTransactionDetailService instance;

    private CardTransactionDetailService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static CardTransactionDetailService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new CardTransactionDetailService(serverConnectionService);
        }
        return instance;
    }

    public static CardTransactionDetailService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public CardTransactionDetail getDetailByTransactionId(Long transactionId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_CARD_DETAIL_BY_TRANSACTION_ID");
        message.addData("transactionId", transactionId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_CARD_DETAIL_BY_TRANSACTION_ID");
            ServerResponseValidator.validateResponse(response);
            logger.info("Detalle de tarjeta obtenido: " + response);
            return CardTransactionDetailUtils.fromMap((Map<String, Object>) response.getData("cardDetail"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al obtener el detalle de tarjeta", e);
        }
    }
}
