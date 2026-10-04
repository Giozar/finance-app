package com.giozar04.walletCardLinks.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.walletCardLinks.application.ports.output.WalletCardLinkGateway;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.infrastructure.transport.socket.ServerConnectionService;
import com.giozar04.serverConnection.infrastructure.transport.socket.ServerResponseValidator;
import com.giozar04.walletCardLinks.infrastructure.serialization.WalletCardLinkMapper;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public class WalletCardLinkService implements WalletCardLinkGateway {

    private final ServerConnectionService serverConnectionService;
    private static final ConsoleLogger logger = ConsoleLogger.getInstance();
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
        message.setType("GET_LINKS_BY_WALLET_ACCOUNT_ID");
        message.addData("walletAccountId", walletAccountId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_LINKS_BY_WALLET_ACCOUNT_ID");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("walletCardLinks");

            List<WalletCardLink> result = new ArrayList<>();
            if (raw instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        result.add(WalletCardLinkMapper.fromMap((Map<String, Object>) map));
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

    @SuppressWarnings("unchecked")
    public WalletCardLink createWalletCardLink(WalletCardLink link) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_WALLET_CARD_LINK");
        message.addData("walletCardLink", WalletCardLinkMapper.toMap(link));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_WALLET_CARD_LINK");
            ServerResponseValidator.validateResponse(response);
            logger.info("Vínculo wallet-tarjeta creado exitosamente: " + response);
            return WalletCardLinkMapper.fromMap((Map<String, Object>) response.getData("walletCardLink"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al esperar respuesta del servidor", e);
        }
    }

    public void deleteWalletCardLinkById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_WALLET_CARD_LINK");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_WALLET_CARD_LINK");
            ServerResponseValidator.validateResponse(response);
            logger.info("Vínculo wallet-tarjeta eliminado exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientOperationException("Error al esperar respuesta del servidor", e);
        }
    }
}

