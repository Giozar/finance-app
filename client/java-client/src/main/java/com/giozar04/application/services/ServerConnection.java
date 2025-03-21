package com.giozar04.application.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.giozar04.json.utils.JsonUtils;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Gestiona la conexión y comunicación con el servidor vía sockets.
 */
public class ServerConnection {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String serverHost;
    private final int serverPort;
    private volatile boolean isConnected;

    public ServerConnection(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.isConnected = false;
    }

    /** Establece la conexión y lanza el hilo de escucha. */
    public void connect() throws IOException {
        socket = new Socket(serverHost, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;
        startListening();
    }

    /** Hilo que escucha mensajes entrantes. */
    private void startListening() {
        new Thread(() -> {
            try {
                while (isConnected) {
                    String line = in.readLine();
                    if (line == null) break; // Fin de stream
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

    /** Procesa el mensaje recibido. */
    private void processIncomingMessage(Message message) {
        System.out.println("Mensaje recibido: " + message);
    }

    /** Envía un mensaje al servidor. */
    public void sendMessage(Message message) throws IOException {
        if (socket != null && !socket.isClosed() && out != null) {
            String json = JsonUtils.messageToJson(message);
            out.println(json);
        } else {
            throw new IOException("No se ha establecido la conexión con el servidor");
        }
    }

    /** Envía una transacción encapsulada en un mensaje. */
    public void sendTransaction(Transaction transaction) throws IOException {
        Message message = new Message();
        message.setType("CREATE_TRANSACTION");
        message.addData("transaction", TransactionUtils.transactionToMap(transaction));
        sendMessage(message);
    }

    /** Cierra la conexión y libera recursos. */
    public void disconnect() throws IOException {
        isConnected = false;
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
    }
}
