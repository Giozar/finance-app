package com.giozar04.walletCardLinks.infrastructure.handlers;

import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.walletCardLinks.application.services.WalletCardLinkService;
import com.giozar04.walletCardLinks.infrastructure.controllers.WalletCardLinkControllers;

public class WalletCardLinkHandlers implements ServerRegisterHandlers {

    private final WalletCardLinkService service;

    public WalletCardLinkHandlers(WalletCardLinkService service) {
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
