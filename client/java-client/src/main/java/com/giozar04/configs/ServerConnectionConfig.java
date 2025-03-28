package com.giozar04.configs;

public class ServerConnectionConfig {

    private final String host;
    private final int port;

    public ServerConnectionConfig() {
        this.host = AppConfig.getProperty("server.host");
        this.port = Integer.parseInt(AppConfig.getProperty("server.port"));
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}

