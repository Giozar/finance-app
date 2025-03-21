package com.giozar04.json.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.giozar04.messages.domain.models.Message;

/**
 * Utilidades para convertir objetos a JSON y viceversa sin librerías externas.
 */
public class JsonUtils {

    public static String messageToJson(Message msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"type\":\"").append(msg.getType() == null ? "" : msg.getType()).append("\",")
          .append("\"content\":\"").append(msg.getContent() == null ? "" : msg.getContent()).append("\",")
          .append("\"status\":\"").append(msg.getStatus() == null ? "PENDING" : msg.getStatus().name()).append("\",")
          .append("\"data\":").append(objectToJson(msg.getData()))
          .append("}");
        return sb.toString();
    }

    public static Message jsonToMessage(String json) {
        Message message = new Message();
        String typeValue = extractJsonField(json, "type");
        message.setType(typeValue);
        String contentValue = extractJsonField(json, "content");
        message.setContent(contentValue);
        String statusValue = extractJsonField(json, "status");
        if (statusValue != null) {
            try {
                message.setStatus(Message.Status.valueOf(statusValue));
            } catch (Exception e) {
                message.setStatus(Message.Status.PENDING);
            }
        } else {
            message.setStatus(Message.Status.PENDING);
        }
        String dataObj = extractJsonObject(json, "data");
        if (dataObj != null && !dataObj.isEmpty()) {
            Map<String, Object> dataMap = parseJsonObject(dataObj);
            message.setData(dataMap);
        }
        return message;
    }

    private static String objectToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + obj + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) return mapToJson((Map<?, ?>) obj);
        if (obj instanceof Iterable) return listToJson((Iterable<?>) obj);
        return "\"" + obj.toString() + "\"";
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey() == null ? "null" : e.getKey().toString()).append("\":")
              .append(objectToJson(e.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listToJson(Iterable<?> list) {
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

    private static String extractJsonField(String json, String fieldName) {
        String search = "\"" + fieldName + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private static String extractJsonObject(String json, String fieldName) {
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

    private static Map<String, Object> parseJsonObject(String json) {
        Map<String, Object> result = new ConcurrentHashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        String[] pairs = splitTopLevelCommas(json);
        for (String pair : pairs) {
            int colonPos = pair.indexOf(":");
            if (colonPos < 0) continue;
            String rawKey = pair.substring(0, colonPos).trim();
            String rawValue = pair.substring(colonPos + 1).trim();
            String key = trimQuotes(rawKey);
            Object value = parseValue(rawValue);
            result.put(key, value);
        }
        return result;
    }

    private static Object parseValue(String rawValue) {
        rawValue = rawValue.trim();
        if (rawValue.startsWith("\"")) return trimQuotes(rawValue);
        else if (rawValue.startsWith("{")) return parseJsonObject(rawValue);
        else if (rawValue.startsWith("[")) return parseJsonArray(rawValue);
        else return rawValue;
    }

    private static Object parseJsonArray(String json) {
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        String[] elems = splitTopLevelCommas(json);
        List<Object> list = new ArrayList<>();
        for (String e : elems) {
            list.add(parseValue(e));
        }
        return list;
    }

    private static String[] splitTopLevelCommas(String json) {
        List<String> result = new ArrayList<>();
        int braceLevel = 0, bracketLevel = 0;
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
        if (current.length() > 0) result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private static String trimQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"")) s = s.substring(1);
        if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);
        return s;
    }
}
