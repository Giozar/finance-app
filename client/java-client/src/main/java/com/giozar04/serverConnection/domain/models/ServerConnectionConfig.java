package com.giozar04.serverConnection.domain.models;

public class ServerConnectionConfig {
    protected String serverHost;
    protected int serverPort;
    protected boolean isConnected;

    public ServerConnectionConfig(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.isConnected = false;
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
