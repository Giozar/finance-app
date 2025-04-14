package com.giozar04.transactions.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.exceptions.TransactionExceptions;

public class TransactionService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
    private static TransactionService instance;

    private TransactionService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static TransactionService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new TransactionService(serverConnectionService);
        }
        return instance;
    }

    public static TransactionService getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public Transaction createTransaction(Transaction transaction) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_TRANSACTION");
        message.addData("transaction", TransactionUtils.transactionToMap(transaction));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_TRANSACTION");
            ServerResponseValidator.validateResponse(response);
            logger.info("Transacción creada exitosamente: " + response);
            return TransactionUtils.mapToTransaction((Map<String, Object>) response.getData("transaction"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionCreationException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Transaction updateTransactionById(Long transactionId, Transaction transaction) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_TRANSACTION");
        message.addData("id", transactionId);
        message.addData("transaction", TransactionUtils.transactionToMap(transaction));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_TRANSACTION");
            ServerResponseValidator.validateResponse(response);
            logger.info("Transacción actualizada correctamente: " + response);
            return TransactionUtils.mapToTransaction((Map<String, Object>) response.getData("transaction"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionUpdateException("Error al esperar la respuesta del servidor", e);
        }
    }

    public void deleteTransactionById(Long transactionId) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_TRANSACTION");
        message.addData("id", transactionId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_TRANSACTION");
            ServerResponseValidator.validateResponse(response);
            logger.info("Transacción eliminada exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionDeletionException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Transaction getTransactionById(Long transactionId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_TRANSACTION");
        message.addData("id", transactionId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_TRANSACTION");
            ServerResponseValidator.validateResponse(response);
            logger.info("Transacción obtenida correctamente: " + response);
            return TransactionUtils.mapToTransaction((Map<String, Object>) response.getData("transaction"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> getAllTransactions() throws ClientOperationException {
        logger.info("Solicitando todas las transacciones...");
        Message message = new Message();
        message.setType("GET_ALL_TRANSACTIONS");

        serverConnectionService.sendMessage(message);

        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_TRANSACTIONS");
            ServerResponseValidator.validateResponse(response);
            Object raw = response.getData("transactions");

            if (raw == null) {
                throw new TransactionExceptions.TransactionRetrievalException("El servidor respondió sin incluir la lista de transacciones", null);
            }

            if (raw instanceof List<?> rawList) {
                List<Transaction> transactions = new ArrayList<>();
                for (Object obj : rawList) {
                    if (obj instanceof Map<?, ?> map) {
                        transactions.add(TransactionUtils.mapToTransaction((Map<String, Object>) map));
                    }
                }
                logger.info("Transacciones obtenidas correctamente. Total: " + transactions.size());
                return transactions;
            } else {
                throw new TransactionExceptions.TransactionParsingException("Formato inesperado: " + raw.getClass().getName(), null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }
}
