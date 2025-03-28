package com.giozar04.serverConnection.domain.interfaces;

import java.io.IOException;

import com.giozar04.messages.domain.models.Message;

public interface ServerConnectionInterface {
    void connect() throws IOException;
    void disconnect() throws IOException;
    void sendMessage(Message message) throws IOException;
}
