package com.giozar04.walletCardLinks.infrastructure.transport.socket;

import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;
import com.giozar04.walletCardLinks.application.ports.input.WalletCardLinkOperations;
import com.giozar04.walletCardLinks.infrastructure.transport.socket.WalletCardLinkControllers;

public class WalletCardLinkHandlers implements ServerRegisterHandlers {

    private final WalletCardLinkOperations service;

    public WalletCardLinkHandlers(WalletCardLinkOperations service) {
        this.service = service;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            WalletCardLinkControllers.WalletCardLinkMessageTypes.CREATE_LINK,
            WalletCardLinkControllers.createLinkController(service)
        );
        server.registerHandler(
            WalletCardLinkControllers.WalletCardLinkMessageTypes.GET_LINK,
            WalletCardLinkControllers.getLinkController(service)
        );
        server.registerHandler(
            WalletCardLinkControllers.WalletCardLinkMessageTypes.UPDATE_LINK,
            WalletCardLinkControllers.updateLinkController(service)
        );
        server.registerHandler(
            WalletCardLinkControllers.WalletCardLinkMessageTypes.DELETE_LINK,
            WalletCardLinkControllers.deleteLinkController(service)
        );
        server.registerHandler(
            WalletCardLinkControllers.WalletCardLinkMessageTypes.GET_ALL_LINKS,
            WalletCardLinkControllers.getAllLinksController(service)
        );
        server.registerHandler(
            WalletCardLinkControllers.WalletCardLinkMessageTypes.GET_LINKS_BY_WALLET,
            WalletCardLinkControllers.getLinksByWalletController(service)
        );
    }
}
