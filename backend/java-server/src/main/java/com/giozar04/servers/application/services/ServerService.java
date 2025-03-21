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

import com.giozar04.servers.domain.exceptions.ServerOperationException;
import com.giozar04.servers.domain.handlers.MessageHandler;
import com.giozar04.servers.domain.models.ClientConnection;
import com.giozar04.servers.domain.models.Message;
import com.giozar04.servers.domain.models.ServerAbstract;
import com.giozar04.shared.logging.CustomLogger;

/**
 * Implementación concreta del servidor de sockets como Singleton.
 * Garantiza que solo exista una instancia del servidor en toda la aplicación.
 * Incluye manejo automático de recursos con ShutdownHook y procesamiento de mensajes.
 */
public class ServerService extends ServerAbstract {

    private static volatile ServerService instance;

    // Mapa para almacenar los clientes conectados
    private final Map<Integer, ClientConnection> connectedClients;

    // Mapa para almacenar los manejadores de mensajes según su tipo
    private final Map<String, MessageHandler> messageHandlers;

    // Flag para verificar si el ShutdownHook ya se ha registrado
    private boolean shutdownHookRegistered = false;

    /**
     * Constructor privado para implementar el patrón Singleton.
     */
    private ServerService(String serverHost, int serverPort, ExecutorService threadPool, CustomLogger logger) {
        super(serverHost, serverPort, threadPool, logger);
        this.connectedClients = new ConcurrentHashMap<>();
        this.messageHandlers = new ConcurrentHashMap<>();
        registerShutdownHook();
    }

    /**
     * Obtiene la instancia única del servidor, creándola si es necesario.
     */
    public static ServerService getInstance(String serverHost, int serverPort, ExecutorService threadPool, CustomLogger logger) {
        if (instance == null) {
            synchronized (ServerService.class) {
                if (instance == null) {
                    instance = new ServerService(serverHost, serverPort, threadPool, logger);
                }
            }
        }
        return instance;
    }

    /**
     * Variante de getInstance utilizando un logger por defecto.
     */
    public static ServerService getInstance(String serverHost, int serverPort, ExecutorService threadPool) {
        return getInstance(serverHost, serverPort, threadPool, new CustomLogger());
    }

    /**
     * Retorna la instancia ya creada o null si aún no se ha inicializado.
     */
    public static ServerService getInstance() {
        return instance;
    }

    /**
     * Registra un manejador para un tipo específico de mensaje.
     */
    public ServerService registerHandler(String messageType, MessageHandler handler) {
        messageHandlers.put(messageType, handler);
        logger.info("Manejador registrado para mensajes de tipo: " + messageType);
        return this;
    }

    /**
     * Elimina el manejador de un tipo específico de mensaje.
     */
    public ServerService unregisterHandler(String messageType) {
        messageHandlers.remove(messageType);
        logger.info("Manejador eliminado para mensajes de tipo: " + messageType);
        return this;
    }

    /**
     * Registra un ShutdownHook para liberar recursos al finalizar la aplicación.
     */
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
                    logger.error("Error durante la liberación de recursos del servidor", e);
                    Thread.currentThread().interrupt();
                }
                logger.info("Servidor detenido y recursos liberados correctamente");
            }));
            shutdownHookRegistered = true;
            logger.info("ShutdownHook registrado para la limpieza de recursos");
        }
    }

    @Override
    public void startServer() throws ServerOperationException, IOException {
        baseStartServer();
        // Lanza un hilo que acepta conexiones de forma continua
        threadPool.submit(() -> {
            try {
                while (isRunning) {
                    acceptClientConnections();
                }
            } catch (ServerOperationException | IOException e) {
                logger.error("Error en el ciclo de aceptación de conexiones: ", e);
            }
        });
        logger.info("Servidor listo para aceptar conexiones entrantes");
    }

    @Override
    public void stopServer() throws ServerOperationException {
        // Cierra las conexiones de cada cliente conectado
        connectedClients.values().forEach(client -> {
            try {
                Socket socket = client.getSocket();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.warn("Error al cerrar la conexión del cliente " + client.getId(), e);
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

    /**
     * Maneja la aceptación de una conexión entrante.
     * Este método acepta una conexión y delega su procesamiento a handleClientConnection.
     */
    @Override
    public void acceptClientConnections() throws ServerOperationException, IOException {
        if (!isServerRunning() || serverSocket == null || serverSocket.isClosed()) {
            throw new ServerOperationException("El servidor no está en ejecución");
        }
        try {
            logger.info("Esperando nueva conexión de cliente...");
            Socket socket = serverSocket.accept();
            // Se utiliza el contador heredado para asignar un ID único
            int clientId = clientIdGenerator.incrementAndGet();
            ClientConnection clientConnection = new ClientConnection(socket, clientId);
            // Delegar el manejo de la conexión
            handleClientConnection(clientConnection);
        } catch (IOException e) {
            if (isServerRunning()) {
                logger.error("Error al aceptar la conexión de un cliente", e);
                throw e;
            } else {
                logger.info("El servidor fue detenido mientras se esperaba una conexión");
            }
        }
    }

    @Override
    public int getConnectedClientsCount() {
        return connectedClients.size();
    }

    /**
     * Maneja la conexión de un cliente.
     * Se registra el cliente y se procesa la comunicación en un hilo separado.
     */
    @Override
    public void handleClientConnection(ClientConnection clientConnection) throws ServerOperationException {
        if (clientConnection == null) {
            logger.warn("Se recibió una conexión nula, ignorando");
            return;
        }
        // Registrar el cliente en el mapa de conectados
        connectedClients.put(clientConnection.getId(), clientConnection);
        logger.info("Cliente " + clientConnection.getId() + " conectado desde " +
                   clientConnection.getSocket().getInetAddress().getHostAddress());

        threadPool.submit(() -> {
            BufferedReader in = null;
            PrintWriter out = null;
            try {
                Socket socket = clientConnection.getSocket();
                // Configurar streams para comunicación de texto
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Enviar mensaje de bienvenida
                Message welcomeMessage = Message.createSuccessMessage("WELCOME",
                        "Conexión establecida. Cliente ID: " + clientConnection.getId());
                // Convertir Message a JSON manualmente
                String welcomeJson = messageToJson(welcomeMessage);
                out.println(welcomeJson);

                // Bucle de comunicación con el cliente
                while (!socket.isClosed() && isRunning) {
                    String receivedJson = in.readLine();
                    if (receivedJson == null) break;
                    Message receivedMessage = jsonToMessage(receivedJson);
                    logger.info("Mensaje recibido del cliente " + clientConnection.getId() +
                               ": " + receivedMessage.getType());
                    processMessage(clientConnection, receivedMessage, out);
                }
            } catch (IOException e) {
                logger.info("Cliente " + clientConnection.getId() + " desconectado: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error inesperado al manejar el cliente " + clientConnection.getId(), e);
            } finally {
                // Liberar recursos y eliminar al cliente del registro
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (!clientConnection.getSocket().isClosed()) {
                        clientConnection.getSocket().close();
                    }
                } catch (IOException e) {
                    logger.error("Error al cerrar recursos del cliente " + clientConnection.getId(), e);
                }
                connectedClients.remove(clientConnection.getId());
                logger.info("Cliente " + clientConnection.getId() + " eliminado del registro");
            }
        });
    }

    /**
     * Procesa un mensaje recibido utilizando el manejador correspondiente.
     */
    private void processMessage(ClientConnection clientConnection, Message message, PrintWriter out) throws IOException {
        String messageType = message.getType();
        MessageHandler handler = messageHandlers.get(messageType);

        if (handler != null) {
            try {
                Message response = handler.handleMessage(clientConnection, message);
                if (response != null) {
                    String jsonResponse = messageToJson(response);
                    out.println(jsonResponse);
                    logger.info("Respuesta enviada al cliente " + clientConnection.getId() +
                               " para mensaje tipo: " + messageType);
                }
            } catch (Exception e) {
                logger.error("Error al procesar mensaje tipo '" + messageType +
                           "' del cliente " + clientConnection.getId(), e);
                Message errorResponse = Message.createErrorMessage(messageType, "Error al procesar solicitud: " + e.getMessage());
                String jsonError = messageToJson(errorResponse);
                out.println(jsonError);
            }
        } else {
            logger.warn("No hay manejador registrado para mensajes de tipo: " + messageType);
            Message errorResponse = Message.createErrorMessage(messageType, "Tipo de mensaje no soportado: " + messageType);
            String jsonError = messageToJson(errorResponse);
            out.println(jsonError);
        }
    }

    /**
     * Convierte un objeto Message a una cadena JSON sin usar librerías externas.
     */
    private String messageToJson(Message msg) {
        // Construcción manual de JSON usando StringBuilder
        // Se asume que los strings no contienen comillas, saltos de línea u otros caracteres especiales
        // Si tuvieran, habría que escaparlos manualmente.
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // "type"
        sb.append("\"type\":\"").append(msg.getType() == null ? "" : msg.getType()).append("\"");
        // "content"
        sb.append(",\"content\":\"").append(msg.getContent() == null ? "" : msg.getContent()).append("\"");
        // "status"
        sb.append(",\"status\":\"").append(msg.getStatus() == null ? "PENDING" : msg.getStatus().name()).append("\"");

        // data
        // Asumiremos data es un Map<String,Object> y convertiremos solo pares key->toString()
        sb.append(",\"data\":{");
        if (msg.getData() != null && !msg.getData().isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, Object> entry : msg.getData().entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":\"");
                sb.append(entry.getValue() == null ? "" : entry.getValue().toString());
                sb.append("\"");
                first = false;
            }
        }
        sb.append("}");

        sb.append("}");
        return sb.toString();
    }

    /**
     * Convierte una cadena JSON a un objeto Message sin usar librerías externas.
     * Se hará una implementación muy básica que asume un formato controlado.
     */
    private Message jsonToMessage(String json) {
        // Buscar valores de "type":"..." etc.
        // Este método es muy frágil e inseguro, pero ejemplifica la idea.
        Message msg = new Message();

        String typeValue = extractJsonField(json, "type");
        msg.setType(typeValue);

        String contentValue = extractJsonField(json, "content");
        msg.setContent(contentValue);

        String statusValue = extractJsonField(json, "status");
        if (statusValue != null) {
            try {
                msg.setStatus(Message.Status.valueOf(statusValue));
            } catch (IllegalArgumentException e) {
                msg.setStatus(Message.Status.PENDING);
            }
        } else {
            msg.setStatus(Message.Status.PENDING);
        }

        // Extraer data como un sub-JSON y luego parsear sus campos key->value (simple)
        String dataJson = extractJsonObject(json, "data");
        if (dataJson != null && !dataJson.isEmpty()) {
            Map<String, Object> dataMap = parseSimpleMap(dataJson);
            msg.setData(dataMap);
        }

        return msg;
    }

    /**
     * Extrae el valor de un campo "fieldName":"value" dentro de un JSON.
     * No es robusto contra anidaciones, comillas escapadas, etc.
     */
    private String extractJsonField(String json, String fieldName) {
        // Buscar "fieldName":" en la cadena y extraer hasta la siguiente "
        String search = "\"" + fieldName + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    /**
     * Extrae un objeto JSON como "fieldName":{ ... }.
     * Retorna el contenido interno de las llaves { } sin el nombre del campo.
     */
    private String extractJsonObject(String json, String fieldName) {
        String search = "\"" + fieldName + "\":{";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        // Buscar la llave de cierre
        int braceCount = 1;
        int pos = start;
        while (pos < json.length() && braceCount > 0) {
            char c = json.charAt(pos);
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            pos++;
        }
        if (braceCount != 0) return null;
        // El contenido es la subcadena de start..pos-2
        return json.substring(start, pos - 1);
    }

    /**
     * Parsea un contenido de la forma "key":"value","key2":"value2" y lo pone en un Map.
     */
    private Map<String, Object> parseSimpleMap(String innerJson) {
        Map<String, Object> result = new ConcurrentHashMap<>();
        // Dividir por comas, luego separar key y value
        // Esto no maneja comas dentro de cadenas, etc.
        // Se asume un formato "key":"value"
        String[] pairs = innerJson.split(",");
        for (String pair : pairs) {
            // Buscar :\"
            int colon = pair.indexOf("\":\"");
            if (colon < 0) continue;
            // key
            String keyRaw = pair.substring(1, colon); // saltamos la primera comilla
            String valRaw = pair.substring(colon + 4, pair.length() - 1); // saltamos ":"
            result.put(keyRaw, valRaw);
        }
        return result;
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
