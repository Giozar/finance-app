package com.giozar04.accountCashbackSettings.infrastructure.handlers;

import com.giozar04.accountCashbackSettings.application.services.AccountCashbackSettingService;
import com.giozar04.accountCashbackSettings.infrastructure.controllers.AccountCashbackSettingControllers;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;

public class AccountCashbackSettingHandlers implements ServerRegisterHandlers {

    private final AccountCashbackSettingService service;

    public AccountCashbackSettingHandlers(AccountCashbackSettingService service) {
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
