package com.giozar04.serverConnection.application.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.giozar04.json.utils.JsonUtils;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.domain.models.ServerConnectionAbstract;

public class ServerConnectionService extends ServerConnectionAbstract {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static ServerConnectionService instance;

    private ServerConnectionService(String host, int port) {
        super(host, port);
    }

    public static ServerConnectionService getInstance(String host, int port) {
        if (instance == null) {
            instance = new ServerConnectionService(host, port);
        }
        return instance;
    }

    @Override
    public void connect() throws IOException {
        socket = new Socket(serverHost, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;
        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (isConnected) {
                    String line = in.readLine();
                    if (line == null) break;
                    Message message = JsonUtils.jsonToMessage(line);
                    if (message != null) {
                        processIncomingMessage(message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error al recibir mensajes: " + e.getMessage());
                isConnected = false;
            }
        }).start();
    }

    @Override
    protected void processIncomingMessage(Message message) {
        System.out.println("Mensaje recibido: " + message);
    }

    @Override
    public void sendMessage(Message message) throws IOException {
        if (socket != null && !socket.isClosed() && out != null) {
            String json = JsonUtils.messageToJson(message);
            out.println(json);
        } else {
            throw new IOException("No se ha establecido la conexión con el servidor");
        }
    }

    @Override
    public void disconnect() throws IOException {
        isConnected = false;
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
    }
}
