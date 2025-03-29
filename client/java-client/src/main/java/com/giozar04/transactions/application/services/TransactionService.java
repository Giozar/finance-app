package com.giozar04.transactions.application.services;

import java.io.IOException;

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
            Message mesage = serverConnectionService.receiveMessage();
            if (message.getType().equals("GET_ALL_TRANSACTIONS")) {
                System.out.println("Respuesta de todas las transacciones:" + mesage.getData("transactions"));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
