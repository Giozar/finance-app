package com.giozar04.application.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.giozar04.servers.domain.models.Message;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClientService se encarga de gestionar la conexión y comunicación con el servidor vía sockets.
 * Mantiene la conexión activa, envía mensajes y escucha respuestas.
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

    /**
     * Establece la conexión con el servidor y lanza el hilo de escucha.
     */
    public void connect() throws IOException {
        socket = new Socket(serverHost, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;
        startListening();
    }

    /**
     * Inicia un hilo que se mantiene escuchando mensajes entrantes del servidor.
     */
    private void startListening() {
        new Thread(() -> {
            try {
                while (isConnected) {
                    String line = in.readLine();
                    if (line == null) {
                        break; // fin de stream
                    }
                    Message message = jsonToMessage(line);
                    if (message != null) {
                        processIncomingMessage(message);
                    }
                }
            } catch (IOException e) {
                // Aquí puedes notificar a la UI o manejar la desconexión de forma controlada.
                System.err.println("Error al recibir mensajes: " + e.getMessage());
                isConnected = false;
            }
        }).start();
    }

    /**
     * Procesa el mensaje recibido del servidor.
     * Aquí podrías invocar callbacks, actualizar la UI, etc.
     */
    private void processIncomingMessage(Message message) {
        // Ejemplo básico: imprimir el mensaje recibido
        System.out.println("Mensaje recibido: " + message);
        // Aquí podrías llamar a un listener o notificar a la capa de presentación.
    }

    /**
     * Envía un mensaje al servidor.
     */
    public void sendMessage(Message message) throws IOException {
        if (socket != null && !socket.isClosed() && out != null) {
            String json = messageToJson(message);
            out.println(json);
        } else {
            throw new IOException("No se ha establecido la conexión con el servidor");
        }
    }

    /**
     * Envuelve una transacción en un mensaje y lo envía al servidor.
     * Se convierte la transacción a Map usando TransactionUtils para cumplir con el contrato del servidor.
     */
    public void sendTransaction(Transaction transaction) throws IOException {
        Message message = new Message();
        message.setType("CREATE_TRANSACTION");
        // Convertir la Transaction a un Map antes de enviarla.
        message.addData("transaction", TransactionUtils.transactionToMap(transaction));
        sendMessage(message);
    }

    /**
     * Cierra la conexión con el servidor y libera recursos.
     */
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

    /**
     * Convierte un objeto Message a una cadena JSON sin usar librerías externas.
     */
    private String messageToJson(Message msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // "type"
        sb.append("\"type\":\"").append(msg.getType() == null ? "" : msg.getType()).append("\"");
        // "content"
        sb.append(",\"content\":\"").append(msg.getContent() == null ? "" : msg.getContent()).append("\"");
        // "status"
        sb.append(",\"status\":\"").append(msg.getStatus() == null ? "PENDING" : msg.getStatus().name()).append("\"");
        // data
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
        Message msg = new Message();
        String typeValue = extractJsonField(json, "type");
        msg.setType(typeValue);
        String contentValue = extractJsonField(json, "content");
        msg.setContent(contentValue);
        String statusValue = extractJsonField(json, "status");
        if (statusValue != null) {
            try {
                msg.setStatus(Message.Status.valueOf(statusValue));
            } catch (Exception e) {
                msg.setStatus(Message.Status.PENDING);
            }
        } else {
            msg.setStatus(Message.Status.PENDING);
        }
        // data
        String dataJson = extractJsonObject(json, "data");
        if (dataJson != null && !dataJson.isEmpty()) {
            Map<String, Object> dataMap = parseSimpleMap(dataJson);
            msg.setData(dataMap);
        }
        return msg;
    }

    /**
     * Extrae el valor de un campo "fieldName":"value" dentro de un JSON.
     * No es robusto contra anidaciones o comillas escapadas.
     */
    private String extractJsonField(String json, String fieldName) {
        String search = "\"" + fieldName + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    /**
     * Extrae un objeto JSON como "fieldName":{ ... } y retorna el contenido interno de las llaves { }.
     */
    private String extractJsonObject(String json, String fieldName) {
        String search = "\"" + fieldName + "\":{";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int braceCount = 1;
        int pos = start;
        while (pos < json.length() && braceCount > 0) {
            char c = json.charAt(pos);
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            pos++;
        }
        if (braceCount != 0) return null;
        return json.substring(start, pos - 1);
    }

    /**
     * Parsea un contenido de la forma "key":"value","key2":"value2" y lo pone en un Map.
     */
    private Map<String, Object> parseSimpleMap(String innerJson) {
        Map<String, Object> result = new ConcurrentHashMap<>();
        String[] pairs = innerJson.split(",");
        for (String pair : pairs) {
            int colon = pair.indexOf("\":\"");
            if (colon < 0) continue;
            String keyRaw = pair.substring(1, colon);
            String valRaw = pair.substring(colon + 4, pair.length() - 1);
            result.put(keyRaw, valRaw);
        }
        return result;
    }
}
