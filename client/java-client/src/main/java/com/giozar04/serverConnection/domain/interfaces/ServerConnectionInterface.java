package com.giozar04.serverConnection.domain.interfaces;

import java.io.IOException;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface ServerConnectionInterface {
    void connect() throws ClientOperationException, IOException;
    void disconnect() throws ClientOperationException, IOException;
    void sendMessage(Message message) throws ClientOperationException, IOException;
    Message receiveMessage() throws InterruptedException, ClientOperationException, IOException;
}
