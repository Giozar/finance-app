package com.giozar04.serverConnection.domain.models;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.domain.interfaces.ServerConnectionInterface;

public abstract class ServerConnectionAbstract extends ServerConnectionConfig implements ServerConnectionInterface {

    public ServerConnectionAbstract(String serverHost, int serverPort) {
        super(serverHost, serverPort);
    }
    protected abstract void processIncomingMessage(Message message);
}
