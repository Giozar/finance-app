package com.giozar04.accounts.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.accounts.infrastructure.serialization.AccountMapper;
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.application.exceptions.AccountCreationException;
import com.giozar04.accounts.application.exceptions.AccountDeletionException;
import com.giozar04.accounts.infrastructure.serialization.AccountParsingException;
import com.giozar04.accounts.application.exceptions.AccountRetrievalException;
import com.giozar04.accounts.application.exceptions.AccountUpdateException;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;


public class AccountService {

    private final ServerConnectionService serverConnectionService;
    private static final ConsoleLogger logger = ConsoleLogger.getInstance();
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

    @SuppressWarnings("unchecked")
    public Account createAccount(Account account) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_ACCOUNT");
        message.addData("account", AccountMapper.toMap(account));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cuenta creada exitosamente: " + response);
            return AccountMapper.fromMap((Map<String, Object>) response.getData("account"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountCreationException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Account updateAccountById(Long id, Account account) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_ACCOUNT");
        message.addData("id", id);
        message.addData("account", AccountMapper.toMap(account));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cuenta actualizada correctamente: " + response);
            return AccountMapper.fromMap((Map<String, Object>) response.getData("account"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountUpdateException("Error al esperar respuesta del servidor", e);
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
            logger.info("Cuenta eliminada exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountDeletionException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Account getAccountById(Long id) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_ACCOUNT");
        message.addData("id", id);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ACCOUNT");
            ServerResponseValidator.validateResponse(response);
            logger.info("Cuenta obtenida correctamente: " + response);
            return AccountMapper.fromMap((Map<String, Object>) response.getData("account"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAllAccounts() throws ClientOperationException {
        logger.info("Solicitando todas las cuentas...");
        Message message = new Message();
        message.setType("GET_ALL_ACCOUNTS");

        serverConnectionService.sendMessage(message);

        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_ACCOUNTS");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("accounts");

            if (raw == null) {
                throw new AccountRetrievalException("Lista de cuentas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Account> accounts = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        accounts.add(AccountMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Cuentas obtenidas correctamente. Total: " + accounts.size());
                return accounts;
            } else {
                throw new AccountParsingException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAccountsByUserId(long userId) throws ClientOperationException {
        logger.info("Solicitando cuentas del usuario " + userId + "...");
        Message message = new Message();
        message.setType("GET_ACCOUNTS_BY_USER");
        message.addData("userId", userId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_ACCOUNTS_BY_USER");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("accounts");

            if (raw == null) {
                throw new AccountRetrievalException("Lista de cuentas vacía", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Account> accounts = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        accounts.add(AccountMapper.fromMap((Map<String, Object>) map));
                    }
                }
                logger.info("Cuentas del usuario obtenidas correctamente. Total: " + accounts.size());
                return accounts;
            } else {
                throw new AccountParsingException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountRetrievalException("Error al esperar respuesta del servidor", e);
        }
    }
}
