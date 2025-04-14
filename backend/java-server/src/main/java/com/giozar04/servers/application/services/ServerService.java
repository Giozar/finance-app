package com.giozar04.servers.application.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.giozar04.json.utils.JsonUtils;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.servers.domain.exceptions.ServerOperationException;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;
import com.giozar04.servers.domain.models.ServerAbstract;

/**
 * Implementa el servidor de sockets como Singleton.
 */
public class ServerService extends ServerAbstract {

    private static volatile ServerService instance;
    private final Map<Integer, ClientConnection> connectedClients;
    private final Map<String, MessageHandler> messageHandlers;
    private boolean shutdownHookRegistered = false;

    private ServerService(String serverHost, int serverPort, ExecutorService threadPool) {
        super(serverHost, serverPort, threadPool);
        this.connectedClients = new ConcurrentHashMap<>();
        this.messageHandlers = new ConcurrentHashMap<>();
        registerShutdownHook();
    }

    public static ServerService getInstance(String serverHost, int serverPort, ExecutorService threadPool) {
        if (instance == null) {
            synchronized (ServerService.class) {
                if (instance == null) {
                    instance = new ServerService(serverHost, serverPort, threadPool);
                }
            }
        }
        return instance;
    }

    /** Registra un manejador para un tipo de mensaje. */
    public ServerService registerHandler(String messageType, MessageHandler handler) {
        messageHandlers.put(messageType, handler);
        logger.info("Manejador registrado para mensajes de tipo: " + messageType);
        return this;
    }

    /** Elimina el manejador de un tipo de mensaje. */
    public ServerService unregisterHandler(String messageType) {
        messageHandlers.remove(messageType);
        logger.info("Manejador eliminado para mensajes de tipo: " + messageType);
        return this;
    }

    /** Registra un ShutdownHook para liberar recursos al cerrar la aplicación. */
    private void registerShutdownHook() {
        if (!shutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Apagando el servidor y liberando recursos...");
                try {
                    if (isServerRunning()) {
                        stopServer();
                    }
                    if (threadPool != null && !threadPool.isShutdown()) {
                        threadPool.shutdown();
                        if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                            threadPool.shutdownNow();
                            if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                                logger.error("El pool de hilos no se cerró correctamente");
                            }
                        }
                    }
                } catch (ServerOperationException | InterruptedException e) {
                    logger.error("Error al liberar recursos del servidor", e);
                    Thread.currentThread().interrupt();
                }
                logger.info("Servidor detenido y recursos liberados");
            }));
            shutdownHookRegistered = true;
            logger.info("ShutdownHook registrado para limpieza de recursos");
        }
    }

    @Override
    public void startServer() throws ServerOperationException, IOException {
        baseStartServer();
        threadPool.submit(() -> {
            try {
                while (isRunning) {
                    acceptClientConnections();
                }
            } catch (ServerOperationException | IOException e) {
                logger.error("Error aceptando conexiones: ", e);
            }
        });
        logger.info("Servidor listo para aceptar conexiones");
    }

    @Override
    public void stopServer() throws ServerOperationException {
        connectedClients.values().forEach(client -> {
            try {
                Socket socket = client.getSocket();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.warn("Error cerrando conexión del cliente " + client.getId(), e);
            }
        });
        connectedClients.clear();
        baseStopServer();
    }

    @Override
    public void restartServer() throws ServerOperationException, IOException {
        logger.info("Reiniciando el servidor...");
        stopServer();
        startServer();
        logger.info("Servidor reiniciado correctamente");
    }

    @Override
    public boolean isServerRunning() {
        return isRunning && serverSocket != null && !serverSocket.isClosed();
    }

    /** Acepta una conexión entrante y la delega a handleClientConnection. */
    @Override
    public void acceptClientConnections() throws ServerOperationException, IOException {
        if (!isServerRunning() || serverSocket == null || serverSocket.isClosed())
            throw new ServerOperationException("El servidor no está en ejecución");
        try {
            logger.info("Esperando conexión de cliente...");
            Socket socket = serverSocket.accept();
            int clientId = clientIdGenerator.incrementAndGet();
            ClientConnection clientConnection = new ClientConnection(socket, clientId);
            handleClientConnection(clientConnection);
        } catch (IOException e) {
            if (isServerRunning()) {
                logger.error("Error al aceptar conexión de cliente", e);
                throw e;
            } else {
                logger.info("Servidor detenido durante la espera de conexión");
            }
        }
    }

    @Override
    public int getConnectedClientsCount() {
        return connectedClients.size();
    }

    /** Maneja la conexión de un cliente en un hilo separado. */
    @Override
    public void handleClientConnection(ClientConnection clientConnection) throws ServerOperationException {
        if (clientConnection == null) {
            logger.warn("Conexión nula recibida, ignorando");
            return;
        }
        connectedClients.put(clientConnection.getId(), clientConnection);
        logger.info("Cliente " + clientConnection.getId() + " conectado desde " +
                   clientConnection.getSocket().getInetAddress().getHostAddress());

        threadPool.submit(() -> {
            BufferedReader in = null;
            PrintWriter out = null;
            try {
                Socket socket = clientConnection.getSocket();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                Message welcomeMessage = Message.createSuccessMessage("WELCOME",
                        "Conexión establecida. Cliente ID: " + clientConnection.getId());
                out.println(JsonUtils.messageToJson(welcomeMessage));

                while (!socket.isClosed() && isRunning) {
                    String receivedJson = in.readLine();
                    if (receivedJson == null) break;
                    Message receivedMessage = JsonUtils.jsonToMessage(receivedJson);
                    logger.info("Mensaje recibido del cliente " + clientConnection.getId() +
                               ": " + receivedMessage.getType());
                    processMessage(clientConnection, receivedMessage, out);
                }
            } catch (IOException e) {
                logger.info("Cliente " + clientConnection.getId() + " desconectado: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error al manejar el cliente " + clientConnection.getId(), e);
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (!clientConnection.getSocket().isClosed()) {
                        clientConnection.getSocket().close();
                    }
                } catch (IOException e) {
                    logger.error("Error cerrando recursos del cliente " + clientConnection.getId(), e);
                }
                connectedClients.remove(clientConnection.getId());
                logger.info("Cliente " + clientConnection.getId() + " eliminado");
            }
        });
    }

    /** Procesa un mensaje usando el manejador registrado correspondiente. */
    private void processMessage(ClientConnection clientConnection, Message message, PrintWriter out) throws IOException {
        String messageType = message.getType();
        MessageHandler handler = messageHandlers.get(messageType);
        if (handler != null) {
            try {
                Message response = handler.handleMessage(clientConnection, message);
                if (response != null) {
                    out.println(JsonUtils.messageToJson(response));
                    logger.info("Respuesta enviada al cliente " + clientConnection.getId() +
                               " para mensaje: " + messageType);
                }
            } catch (Exception e) {
                logger.error("Error procesando mensaje tipo '" + messageType +
                           "' del cliente " + clientConnection.getId(), e);
                Message errorResponse = Message.createErrorMessage(messageType, "Error al procesar solicitud: " + e.getMessage());
                out.println(JsonUtils.messageToJson(errorResponse));
            }
        } else {
            logger.warn("Sin manejador para mensaje de tipo: " + messageType);
            Message errorResponse = Message.createErrorMessage(messageType, "Tipo de mensaje no soportado: " + messageType);
            out.println(JsonUtils.messageToJson(errorResponse));
        }
    }

    @Override
    public void close() throws Exception {
        try {
            stopServer();
        } catch (ServerOperationException e) {
            logger.error("Error al cerrar el servidor", e);
            throw e;
        }
    }
}
