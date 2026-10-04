package com.giozar04.accountCashbackSettings.infrastructure.transport.socket;

import com.giozar04.accountCashbackSettings.application.ports.input.AccountCashbackSettingOperations;
import com.giozar04.accountCashbackSettings.infrastructure.transport.socket.AccountCashbackSettingControllers;
import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;

public class AccountCashbackSettingHandlers implements ServerRegisterHandlers {

    private final AccountCashbackSettingOperations service;

    public AccountCashbackSettingHandlers(AccountCashbackSettingOperations service) {
        this.service = service;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            AccountCashbackSettingControllers.MessageTypes.CREATE_ACCOUNT_CASHBACK_SETTING,
            AccountCashbackSettingControllers.createSettingController(service)
        );
        server.registerHandler(
            AccountCashbackSettingControllers.MessageTypes.GET_ACCOUNT_CASHBACK_SETTING,
            AccountCashbackSettingControllers.getSettingController(service)
        );
        server.registerHandler(
            AccountCashbackSettingControllers.MessageTypes.UPDATE_ACCOUNT_CASHBACK_SETTING,
            AccountCashbackSettingControllers.updateSettingController(service)
        );
        server.registerHandler(
            AccountCashbackSettingControllers.MessageTypes.DELETE_ACCOUNT_CASHBACK_SETTING,
            AccountCashbackSettingControllers.deleteSettingController(service)
        );
        server.registerHandler(
            AccountCashbackSettingControllers.MessageTypes.GET_ALL_ACCOUNT_CASHBACK_SETTINGS,
            AccountCashbackSettingControllers.getAllSettingsController(service)
        );
    }
}
