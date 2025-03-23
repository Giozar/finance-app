package com.giozar04.serverConnection.domain.models;

import java.io.IOException;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.domain.interfaces.ServerConnectionInterface;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;

public abstract class ServerConnectionAbstract extends ServerConnectionConfig implements ServerConnectionInterface {

    public ServerConnectionAbstract(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }

    @Override
    public void sendTransaction(Transaction transaction) throws IOException {
        Message message = new Message();
        message.setType("CREATE_TRANSACTION");
        message.addData("transaction", TransactionUtils.transactionToMap(transaction));
        sendMessage(message);
    }

    protected abstract void processIncomingMessage(Message message);
}
