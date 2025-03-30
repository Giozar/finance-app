package com.giozar04.transactions.application.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;

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

    public void createTransaction(Transaction transaction) throws IOException {
        Message message = new Message();
        message.setType("CREATE_TRANSACTION");
        message.addData("transaction", TransactionUtils.transactionToMap(transaction));
        serverConnectionService.sendMessage(message);
    }

    public void getAllTransactions() throws IOException {
        Message message = new Message();
        message.setType("GET_ALL_TRANSACTIONS");
        serverConnectionService.sendMessage(message);

        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_TRANSACTIONS");
            Object raw = response.getData("transactions");

            System.out.println("🟨 Datos crudos recibidos: " + raw);
            System.out.println("🟨 Tipo recibido: " + raw.getClass().getName());
            System.out.println("[CLIENT] Mensaje recibido del servidor: " + response);

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
                // Mostrar las transacciones
                for (Transaction t : transactions) {
                    System.out.println("- " + t.getId() + " | " + t.getTitle() + " | $" + t.getAmount() + " | " + t.getDate());
                }
            } else {
                System.out.println("Formato inesperado: " + raw.getClass().getName());
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
