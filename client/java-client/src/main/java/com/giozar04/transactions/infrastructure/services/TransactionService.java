package com.giozar04.transactions.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import com.giozar04.transactions.domain.exceptions.TransactionExceptions;

public class TransactionService {

    private final ServerConnectionService serverConnectionService;
    private static TransactionService instance;

    public TransactionService(ServerConnectionService serverConnectionService) {
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

    public void createTransaction(Transaction transaction) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_TRANSACTION");
        message.addData("transaction", TransactionUtils.transactionToMap(transaction));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_TRANSACTION");
            System.out.println("[CLIENT] Mensaje recibido del servidor: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionCreationException("Error al esperar la respuesta del servidor", e);
        }
    }

    public void updateTransactionById(Long transactionId, Transaction transaction) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_TRANSACTION");
        // Modificado: enviar el ID con la clave "id"
        message.addData("id", transactionId);
        message.addData("transaction", TransactionUtils.transactionToMap(transaction));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_TRANSACTION");
            System.out.println("[CLIENT] Mensaje recibido del servidor: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionUpdateException("Error al esperar la respuesta del servidor", e);
        }
    }

    public void deleteTransactionById(Long transactionId) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_TRANSACTION");
        // Modificado: enviar el ID con la clave "id"
        message.addData("id", transactionId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_TRANSACTION");
            System.out.println("[CLIENT] Mensaje recibido del servidor: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionDeletionException("Error al esperar la respuesta del servidor", e);
        }
    }

    public void getTransactionById(Long transactionId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_TRANSACTION");
        // Modificado: enviar el ID con la clave "id"
        message.addData("id", transactionId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_TRANSACTION");
            System.out.println("[CLIENT] Mensaje recibido del servidor: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }

    public List<Transaction> getAllTransactions() throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_ALL_TRANSACTIONS");

        serverConnectionService.sendMessage(message);

        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_TRANSACTIONS");
            Object raw = response.getData("transactions");

            if (raw == null) {
                throw new TransactionExceptions.TransactionRetrievalException(
                        "El servidor respondió sin incluir la lista de transacciones", null
                );
            }

            // System.out.println("🟨 Datos crudos recibidos: " + raw);
            // System.out.println("🟨 Tipo recibido: " + raw.getClass().getName());
            // System.out.println("[CLIENT] Mensaje recibido del servidor: " + response);

            if (raw instanceof List) {
                List<?> transactionList = (List<?>) raw;
                List<Transaction> transactions = new ArrayList<>();
                for (Object obj : transactionList) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) obj;
                        Transaction transaction = TransactionUtils.mapToTransaction(map);
                        transactions.add(transaction);
                    } else {
                        System.out.println("Elemento no esperado en la lista: " + obj);
                    }
                }

                // for (Transaction t : transactions) {
                //     System.out.println("- " + t.getId() + " | " + t.getTitle() + " | $" + t.getAmount() + " | " + t.getDate());
                // }

                return transactions;
            } else {
                System.out.println("Formato inesperado: " + raw.getClass().getName());
                throw new TransactionExceptions.TransactionParsingException(
                        "El servidor devolvió un formato inesperado: " + raw.getClass().getName(), null
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionExceptions.TransactionRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }

}
