package com.giozar04.application.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.transactions.application.utils.TransactionUtils;
import com.giozar04.transactions.domain.entities.Transaction;

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
        System.out.println("Mensaje recibido: " + message);
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
     * Convierte un objeto Message a una cadena JSON sin usar librerías externas,
     * de forma recursiva para sub-Map y List.
     */
    private String messageToJson(Message msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"type\":\"")
          .append(msg.getType() == null ? "" : msg.getType())
          .append("\",");

        sb.append("\"content\":\"")
          .append(msg.getContent() == null ? "" : msg.getContent())
          .append("\",");

        sb.append("\"status\":\"")
          .append(msg.getStatus() == null ? "PENDING" : msg.getStatus().name())
          .append("\",");

        sb.append("\"data\":");
        sb.append(objectToJson(msg.getData()));

        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializa recursivamente un objeto (Map, List, String, Number, etc.) a JSON.
     */
    private String objectToJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }
        if (obj instanceof Number) {
            return obj.toString();
        }
        if (obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map) {
            return mapToJson((Map<?,?>) obj);
        }
        if (obj instanceof Iterable) {
            return listToJson((Iterable<?>) obj);
        }
        return "\"" + obj.toString() + "\"";
    }

    private String mapToJson(Map<?,?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<?,?> e : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"")
              .append(e.getKey() == null ? "null" : e.getKey().toString())
              .append("\":");
            sb.append(objectToJson(e.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String listToJson(Iterable<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Object elem : list) {
            if (!first) sb.append(",");
            sb.append(objectToJson(elem));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convierte una cadena JSON a un objeto Message sin usar librerías externas.
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

        String dataObj = extractJsonObject(json, "data");
        if (dataObj != null && !dataObj.isEmpty()) {
            Map<String, Object> dataMap = parseJsonObject(dataObj);
            msg.setData(dataMap);
        }
        return msg;
    }

    // ============= Métodos de parsing JSON (similares al servidor) =============
    private String extractJsonField(String json, String fieldName) {
        String search = "\"" + fieldName + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

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

    private Map<String, Object> parseJsonObject(String json) {
        Map<String,Object> result = new ConcurrentHashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length()-1);

        String[] pairs = splitTopLevelCommas(json);
        for (String pair : pairs) {
            int colonPos = pair.indexOf(":");
            if (colonPos < 0) continue;
            String rawKey = pair.substring(0, colonPos).trim();
            String rawValue = pair.substring(colonPos+1).trim();

            String key = trimQuotes(rawKey);
            Object value = parseValue(rawValue);
            result.put(key, value);
        }
        return result;
    }

    private Object parseValue(String rawValue) {
        rawValue = rawValue.trim();
        if (rawValue.startsWith("\"")) {
            return trimQuotes(rawValue);
        } else if (rawValue.startsWith("{")) {
            return parseJsonObject(rawValue);
        } else if (rawValue.startsWith("[")) {
            return parseJsonArray(rawValue);
        } else {
            return rawValue;
        }
    }

    private Object parseJsonArray(String json) {
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length()-1);

        String[] elems = splitTopLevelCommas(json);
        java.util.List<Object> list = new java.util.ArrayList<>();
        for (String e : elems) {
            list.add(parseValue(e));
        }
        return list;
    }

    private String[] splitTopLevelCommas(String json) {
        java.util.List<String> result = new java.util.ArrayList<>();
        int braceLevel = 0;
        int bracketLevel = 0;
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (!inQuotes) {
                if (c == '{') { braceLevel++; current.append(c); }
                else if (c == '}') { braceLevel--; current.append(c); }
                else if (c == '[') { bracketLevel++; current.append(c); }
                else if (c == ']') { bracketLevel--; current.append(c); }
                else if (c == ',' && braceLevel == 0 && bracketLevel == 0) {
                    result.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result.toArray(new String[0]);
    }

    private String trimQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"")) s = s.substring(1);
        if (s.endsWith("\"")) s = s.substring(0, s.length()-1);
        return s;
    }
}
