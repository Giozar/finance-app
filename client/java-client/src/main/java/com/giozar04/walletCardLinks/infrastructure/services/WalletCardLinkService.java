package com.giozar04.walletCardLinks.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;
import com.giozar04.walletCardLinks.application.utils.WalletCardLinkUtils;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public class WalletCardLinkService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static WalletCardLinkService instance;

    private WalletCardLinkService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static WalletCardLinkService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new WalletCardLinkService(serverConnectionService);
        }
        return instance;
    }

    public static WalletCardLinkService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public List<WalletCardLink> getAllByWalletId(Long walletAccountId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_WALLET_CARD_LINKS_BY_WALLET_ID");
        message.addData("walletAccountId", walletAccountId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_WALLET_CARD_LINKS_BY_WALLET_ID");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("walletCardLinks");

            List<WalletCardLink> result = new ArrayList<>();
            if (raw instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        result.add(WalletCardLinkUtils.fromMap((Map<String, Object>) map));
                    }
                }
            }

            logger.info("Vínculos obtenidos correctamente para wallet ID: " + walletAccountId);
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al obtener vínculos de tarjeta", e);
        }
    }
}
