package com.giozar04.bankClients.infrastructure.handlers;

import com.giozar04.bankClients.application.services.BankClientService;
import com.giozar04.bankClients.infrastructure.controllers.BankClientControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class BankClientHandlers implements ServerRegisterHandlers {

    private final BankClientService service;

    public BankClientHandlers(BankClientService service) {
        this.service = service;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(BankClientControllers.BankClientMessageTypes.CREATE_BANK_CLIENT,
                BankClientControllers.createBankClientController(service));

        server.registerHandler(BankClientControllers.BankClientMessageTypes.GET_BANK_CLIENT,
                BankClientControllers.getBankClientController(service));

        server.registerHandler(BankClientControllers.BankClientMessageTypes.GET_BANK_CLIENTS_BY_USER,
                BankClientControllers.getBankClientsByUserController(service));

        server.registerHandler(BankClientControllers.BankClientMessageTypes.UPDATE_BANK_CLIENT,
                BankClientControllers.updateBankClientController(service));

        server.registerHandler(BankClientControllers.BankClientMessageTypes.DELETE_BANK_CLIENT,
                BankClientControllers.deleteBankClientController(service));

        server.registerHandler(BankClientControllers.BankClientMessageTypes.GET_ALL_BANK_CLIENTS,
                BankClientControllers.getAllBankClientsController(service));
    }
}
