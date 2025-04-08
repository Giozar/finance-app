package com.giozar04.accounts.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.accounts.application.utils.AccountUtils;
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.exceptions.AccountExceptions;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;

public class AccountService {

    private final ServerConnectionService serverConnectionService;
    private static AccountService instance;

    private AccountService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static AccountService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new AccountService(serverConnectionService);
        }
        return instance;
    }

    public static AccountService getInstance() {
        return instance;
    }

    public void createAccount(Account account) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_ACCOUNT");
        message.addData("account", AccountUtils.accountToMap(account));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            System.out.println("[CLIENT] Cuenta creada: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountExceptions.AccountCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    public void updateAccountById(Long id, Account account) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_ACCOUNT");
        message.addData("id", id);
        message.addData("account", AccountUtils.accountToMap(account));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            System.out.println("[CLIENT] Cuenta actualizada: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountExceptions.AccountUpdateException("Error al esperar respuesta del servidor", e);
        }
    }

    public void deleteAccountById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_ACCOUNT");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            System.out.println("[CLIENT] Cuenta eliminada: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountExceptions.AccountDeletionException("Error al esperar respuesta del servidor", e);
        }
    }

    public void getAccountById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_ACCOUNT");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            System.out.println("[CLIENT] Cuenta recibida: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountExceptions.AccountRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAllAccounts() throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_ALL_ACCOUNTS");

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_ACCOUNTS");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("accounts");

            if (raw == null) {
                throw new AccountExceptions.AccountRetrievalException("Lista de cuentas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Account> accounts = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        accounts.add(AccountUtils.mapToAccount((Map<String, Object>) map));
                    }
                }
                return accounts;
            } else {
                throw new AccountExceptions.AccountParsingException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountExceptions.AccountRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
