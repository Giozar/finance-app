package com.giozar04.walletTransactionDetails.infrastructure.transport.socket;

import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;
import com.giozar04.walletTransactionDetails.application.ports.input.WalletTransactionDetailOperations;
import com.giozar04.walletTransactionDetails.infrastructure.transport.socket.WalletTransactionDetailControllers;

public class WalletTransactionDetailHandlers implements ServerRegisterHandlers {

    private final WalletTransactionDetailOperations service;

    public WalletTransactionDetailHandlers(WalletTransactionDetailOperations service) {
        this.service = service;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            WalletTransactionDetailControllers.MessageTypes.CREATE_DETAIL,
            WalletTransactionDetailControllers.createDetailController(service)
        );
        server.registerHandler(
            WalletTransactionDetailControllers.MessageTypes.GET_DETAIL,
            WalletTransactionDetailControllers.getDetailController(service)
        );
        server.registerHandler(
            WalletTransactionDetailControllers.MessageTypes.UPDATE_DETAIL,
            WalletTransactionDetailControllers.updateDetailController(service)
        );
        server.registerHandler(
            WalletTransactionDetailControllers.MessageTypes.DELETE_DETAIL,
            WalletTransactionDetailControllers.deleteDetailController(service)
        );
        server.registerHandler(
            WalletTransactionDetailControllers.MessageTypes.GET_ALL_DETAILS,
            WalletTransactionDetailControllers.getAllDetailsController(service)
        );
        server.registerHandler(
            WalletTransactionDetailControllers.MessageTypes.GET_DETAILS_BY_TRANSACTION,
            WalletTransactionDetailControllers.getDetailsByTransactionController(service)
        );
    }
}
