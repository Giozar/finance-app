package com.giozar04.walletTransactionDetails.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;
import com.giozar04.walletTransactionDetails.application.utils.WalletTransactionDetailUtils;
import com.giozar04.walletTransactionDetails.domain.entities.WalletTransactionDetail;

public class WalletTransactionDetailService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static WalletTransactionDetailService instance;

    private WalletTransactionDetailService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static WalletTransactionDetailService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new WalletTransactionDetailService(serverConnectionService);
        }
        return instance;
    }

    public static WalletTransactionDetailService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public List<WalletTransactionDetail> getDetailsByTransactionId(Long transactionId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_WALLET_DETAILS_BY_TRANSACTION_ID");
        message.addData("transactionId", transactionId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_WALLET_DETAILS_BY_TRANSACTION_ID");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("walletDetails");

            List<WalletTransactionDetail> result = new ArrayList<>();
            if (raw instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        result.add(WalletTransactionDetailUtils.fromMap((Map<String, Object>) map));
                    }
                }
            }

            logger.info("Detalles de transacción wallet obtenidos. Total: " + result.size());
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al obtener detalles de transacción wallet", e);
        }
    }
}
