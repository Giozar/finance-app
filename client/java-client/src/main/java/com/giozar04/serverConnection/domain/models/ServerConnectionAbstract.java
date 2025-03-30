package com.giozar04.serverConnection.domain.models;

import com.giozar04.configs.ServerConnectionConfig;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.domain.interfaces.ServerConnectionInterface;

public abstract class ServerConnectionAbstract implements ServerConnectionInterface {

    protected String serverHost;
    protected int serverPort;
    protected boolean isConnected;

    public ServerConnectionAbstract(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public ServerConnectionAbstract(ServerConnectionConfig config) {
        this(config.getHost(), config.getPort());
    }

    protected abstract void processIncomingMessage(Message message);
}
