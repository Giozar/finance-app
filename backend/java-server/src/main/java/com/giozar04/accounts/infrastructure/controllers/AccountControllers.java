package com.giozar04.accounts.infrastructure.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.accounts.application.services.AccountService;
import com.giozar04.accounts.infrastructure.serialization.AccountMapper;
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;

public class AccountControllers {

    private static final ConsoleLogger LOGGER = ConsoleLogger.getInstance();

    public static final class AccountMessageTypes {
        public static final String CREATE_ACCOUNT = "CREATE_ACCOUNT";
        public static final String GET_ACCOUNT = "GET_ACCOUNT";
        public static final String UPDATE_ACCOUNT = "UPDATE_ACCOUNT";
        public static final String DELETE_ACCOUNT = "DELETE_ACCOUNT";
        public static final String GET_ALL_ACCOUNTS = "GET_ALL_ACCOUNTS";
        public static final String GET_ACCOUNTS_BY_USER = "GET_ACCOUNTS_BY_USER";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createAccountController(AccountService accountService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de creación de cuenta");

            Map<String, Object> data = (Map<String, Object>) message.getData("account");
            if (data == null) {
                return Message.createErrorMessage(AccountMessageTypes.CREATE_ACCOUNT,
                        "Datos de cuenta no proporcionados");
            }

            Account account = AccountMapper.fromMap(data);
            Account created = accountService.createAccount(account);

            Message response = Message.createSuccessMessage(AccountMessageTypes.CREATE_ACCOUNT,
                    "Cuenta creada exitosamente");
            response.addData("account", AccountMapper.toMap(created));
            return response;
        };
    }

    public static MessageHandler getAccountController(AccountService accountService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de cuenta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(AccountMessageTypes.GET_ACCOUNT,
                        "ID de cuenta inválido o no proporcionado");
            }

            Account account = accountService.getAccountById(id);
            Message response = Message.createSuccessMessage(AccountMessageTypes.GET_ACCOUNT,
                    "Cuenta obtenida exitosamente");
            response.addData("account", AccountMapper.toMap(account));
            return response;
        };
    }

    public static MessageHandler updateAccountController(AccountService accountService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de actualización de cuenta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(AccountMessageTypes.UPDATE_ACCOUNT,
                        "ID de cuenta inválido o no proporcionado");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData("account");
            if (data == null) {
                return Message.createErrorMessage(AccountMessageTypes.UPDATE_ACCOUNT,
                        "Datos de cuenta no proporcionados");
            }

            Account account = AccountMapper.fromMap(data);
            Account updated = accountService.updateAccountById(id, account);

            Message response = Message.createSuccessMessage(AccountMessageTypes.UPDATE_ACCOUNT,
                    "Cuenta actualizada exitosamente");
            response.addData("account", AccountMapper.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteAccountController(AccountService accountService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de eliminación de cuenta");

            Long id = parseId(message.getData("id"));
            if (id == null) {
                return Message.createErrorMessage(AccountMessageTypes.DELETE_ACCOUNT,
                        "ID de cuenta inválido o no proporcionado");
            }

            accountService.deleteAccountById(id);

            return Message.createSuccessMessage(AccountMessageTypes.DELETE_ACCOUNT,
                    "Cuenta eliminada exitosamente");
        };
    }

    public static MessageHandler getAllAccountsController(AccountService accountService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de todas las cuentas");

            List<Account> accounts = accountService.getAllAccounts();
            List<Map<String, Object>> list = new ArrayList<>();

            for (Account account : accounts) {
                list.add(AccountMapper.toMap(account));
            }

            Message response = Message.createSuccessMessage(AccountMessageTypes.GET_ALL_ACCOUNTS,
                    "Cuentas obtenidas exitosamente");
            response.addData("accounts", list);
            response.addData("count", list.size());

            return response;
        };
    }

    public static MessageHandler getAccountsByUserController(AccountService accountService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de cuentas por usuario");

            Long userId = parseId(message.getData("userId"));
            if (userId == null) {
                return Message.createErrorMessage(AccountMessageTypes.GET_ACCOUNTS_BY_USER, "userId inválido");
            }

            List<Account> items = accountService.getAccountsByUserId(userId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Account item : items) {
                result.add(AccountMapper.toMap(item));
            }

            Message response = Message.createSuccessMessage(AccountMessageTypes.GET_ACCOUNTS_BY_USER, "Cuentas del usuario obtenidas exitosamente");
            response.addData("accounts", result);
            response.addData("count", result.size());

            return response;
        };
    }

    private static Long parseId(Object rawId) {
        if (rawId instanceof Long l) return l;
        if (rawId instanceof String s) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
