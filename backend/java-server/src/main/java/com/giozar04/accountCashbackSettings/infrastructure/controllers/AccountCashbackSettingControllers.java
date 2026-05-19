package com.giozar04.accountCashbackSettings.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.accountCashbackSettings.application.services.AccountCashbackSettingService;
import com.giozar04.accountCashbackSettings.application.utils.AccountCashbackSettingUtils;
import com.giozar04.accountCashbackSettings.domain.entities.AccountCashbackSetting;
import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;

public class AccountCashbackSettingControllers {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public static final class MessageTypes {
        public static final String CREATE_ACCOUNT_CASHBACK_SETTING  = "CREATE_ACCOUNT_CASHBACK_SETTING";
        public static final String GET_ACCOUNT_CASHBACK_SETTING     = "GET_ACCOUNT_CASHBACK_SETTING";
        public static final String UPDATE_ACCOUNT_CASHBACK_SETTING  = "UPDATE_ACCOUNT_CASHBACK_SETTING";
        public static final String DELETE_ACCOUNT_CASHBACK_SETTING  = "DELETE_ACCOUNT_CASHBACK_SETTING";
        public static final String GET_ALL_ACCOUNT_CASHBACK_SETTINGS = "GET_ALL_ACCOUNT_CASHBACK_SETTINGS";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createSettingController(AccountCashbackSettingService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Creando configuración de cashback");

            Map<String, Object> data = (Map<String, Object>) message.getData("accountCashbackSetting");
            if (data == null) {
                return Message.createErrorMessage(MessageTypes.CREATE_ACCOUNT_CASHBACK_SETTING,
                        "Datos no proporcionados");
            }

            AccountCashbackSetting setting = AccountCashbackSettingUtils.fromMap(data);
            AccountCashbackSetting created = service.createAccountCashbackSetting(setting);

            Message response = Message.createSuccessMessage(MessageTypes.CREATE_ACCOUNT_CASHBACK_SETTING,
                    "Configuración de cashback creada");
            response.addData("accountCashbackSetting", AccountCashbackSettingUtils.toMap(created));
            return response;
        };
    }

    public static MessageHandler getSettingController(AccountCashbackSettingService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo configuración de cashback por accountId");

            Long accountId = parseId(message.getData("accountId"));
            if (accountId == null) {
                return Message.createErrorMessage(MessageTypes.GET_ACCOUNT_CASHBACK_SETTING,
                        "accountId inválido");
            }

            AccountCashbackSetting setting = service.getAccountCashbackSettingByAccountId(accountId);

            Message response = Message.createSuccessMessage(MessageTypes.GET_ACCOUNT_CASHBACK_SETTING,
                    "Configuración de cashback obtenida");
            response.addData("accountCashbackSetting", AccountCashbackSettingUtils.toMap(setting));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateSettingController(AccountCashbackSettingService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Actualizando configuración de cashback");

            Long accountId = parseId(message.getData("accountId"));
            if (accountId == null) {
                return Message.createErrorMessage(MessageTypes.UPDATE_ACCOUNT_CASHBACK_SETTING,
                        "accountId inválido");
            }

            Map<String, Object> data = (Map<String, Object>) message.getData("accountCashbackSetting");
            if (data == null) {
                return Message.createErrorMessage(MessageTypes.UPDATE_ACCOUNT_CASHBACK_SETTING,
                        "Datos no proporcionados");
            }

            AccountCashbackSetting updated = service.updateAccountCashbackSettingByAccountId(
                    accountId, AccountCashbackSettingUtils.fromMap(data));

            Message response = Message.createSuccessMessage(MessageTypes.UPDATE_ACCOUNT_CASHBACK_SETTING,
                    "Configuración de cashback actualizada");
            response.addData("accountCashbackSetting", AccountCashbackSettingUtils.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteSettingController(AccountCashbackSettingService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Eliminando configuración de cashback");

            Long accountId = parseId(message.getData("accountId"));
            if (accountId == null) {
                return Message.createErrorMessage(MessageTypes.DELETE_ACCOUNT_CASHBACK_SETTING,
                        "accountId inválido");
            }

            service.deleteAccountCashbackSettingByAccountId(accountId);
            return Message.createSuccessMessage(MessageTypes.DELETE_ACCOUNT_CASHBACK_SETTING,
                    "Configuración de cashback eliminada");
        };
    }

    public static MessageHandler getAllSettingsController(AccountCashbackSettingService service) {
        return (ClientConnection client, Message message) -> {
            LOGGER.info("Obteniendo todas las configuraciones de cashback");

            List<AccountCashbackSetting> settings = service.getAllAccountCashbackSettings();
            List<Map<String, Object>> result = new ArrayList<>();

            for (AccountCashbackSetting s : settings) {
                result.add(AccountCashbackSettingUtils.toMap(s));
            }

            Message response = Message.createSuccessMessage(MessageTypes.GET_ALL_ACCOUNT_CASHBACK_SETTINGS,
                    "Configuraciones de cashback obtenidas");
            response.addData("accountCashbackSettings", result);
            response.addData("count", result.size());
            return response;
        };
    }

    private static Long parseId(Object raw) {
        if (raw instanceof Long l) return l;
        if (raw instanceof Number n) return n.longValue();
        if (raw instanceof String s) {
            try { return Long.valueOf(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
