package com.giozar04.serverConnection.application.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.giozar04.json.utils.JsonUtils;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.domain.models.ServerConnectionAbstract;

public class ServerConnectionService extends ServerConnectionAbstract {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static ServerConnectionService instance;

    // Cola de mensajes por tipo
    private final Map<String, BlockingQueue<Message>> messageQueues = new ConcurrentHashMap<>();

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
                    if (line == null) {
                        break;
                    }

                    Message message = JsonUtils.jsonToMessage(line);
                    if (message != null) {
                        processIncomingMessage(message); // extensible para el futuro

                        // Encolar el mensaje en la cola correspondiente a su tipo
                        String type = message.getType();
                        messageQueues
                                .computeIfAbsent(type, k -> new LinkedBlockingQueue<>())
                                .offer(message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error al recibir mensajes: " + e.getMessage());
                isConnected = false;
            }
        }).start();
    }

    /**
     * Método para obtener un mensaje por tipo. Se bloquea hasta recibir el
     * mensaje solicitado.
     */
    public Message waitForMessage(String type) throws InterruptedException {
        BlockingQueue<Message> queue = messageQueues
                .computeIfAbsent(type, k -> new LinkedBlockingQueue<>());
        return queue.take(); // Espera el mensaje de ese tipo
    }

    @Override
    protected void processIncomingMessage(Message message) {
        // Puedes expandir esto en el futuro para manejar otros tipos globales como NOTIFICATIONS
        System.out.println("[CLIENT] Mensaje recibido del servidor: " + message);
    }

    @Override
    public void sendMessage(Message message) throws ClientOperationException {
        if (socket != null && !socket.isClosed() && out != null) {
            try {
                String json = JsonUtils.messageToJson(message);
                out.println(json);
            } catch (Exception e) {
                throw new ClientOperationException("Error al enviar el mensaje: " + e.getMessage(), e);
            }
        } else {
            throw new ClientOperationException("No se ha establecido la conexión con el servidor");
        }
    }

    @Override
    public Message receiveMessage() throws InterruptedException {
        throw new UnsupportedOperationException("Usa waitForMessage(type) en lugar de receiveMessage()");
    }

    @Override
    public void disconnect() throws IOException {
        isConnected = false;
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (socket != null) {
            socket.close();
        }
    }
}
