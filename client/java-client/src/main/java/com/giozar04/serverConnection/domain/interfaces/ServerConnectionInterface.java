package com.giozar04.serverConnection.domain.interfaces;

import java.io.IOException;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.transactions.domain.entities.Transaction;

public interface ServerConnectionInterface {
    void connect() throws IOException;
    void disconnect() throws IOException;
    void sendMessage(Message message) throws IOException;
    void sendTransaction(Transaction transaction) throws IOException;
}
