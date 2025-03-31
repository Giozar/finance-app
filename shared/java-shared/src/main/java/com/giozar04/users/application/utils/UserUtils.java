package com.giozar04.users.application.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.giozar04.users.domain.entities.User;

public class UserUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public static Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("password", user.getPassword()); // ⚠️ Considera omitir esto para respuestas si no es necesario
        map.put("globalBalance", user.getGlobalBalance());
        if (user.getCreatedAt() != null) {
            map.put("createdAt", user.getCreatedAt().format(FORMATTER));
        }
        if (user.getUpdatedAt() != null) {
            map.put("updatedAt", user.getUpdatedAt().format(FORMATTER));
        }
        return map;
    }

    public static User mapToUser(Map<String, Object> map) {
        User user = new User();

        if (map.containsKey("id")) {
            Object id = map.get("id");
            if (id instanceof Number number) {
                user.setId(number.longValue());
            } else if (id instanceof String string) {
                try {
                    user.setId(Long.parseLong(string));
                } catch (NumberFormatException e) {
                    user.setId(0); // o dejar como está
                }
            }
        }

        user.setName((String) map.getOrDefault("name", ""));
        user.setEmail((String) map.getOrDefault("email", ""));
        user.setPassword((String) map.getOrDefault("password", ""));
        
        Object balance = map.get("globalBalance");
        if (balance instanceof Number number) {
            user.setGlobalBalance(number.doubleValue());
        } else if (balance instanceof String) {
            try {
                user.setGlobalBalance(Double.parseDouble((String) balance));
            } catch (NumberFormatException e) {
                user.setGlobalBalance(0.0);
            }
        }

        if (map.containsKey("createdAt")) {
            try {
                user.setCreatedAt(ZonedDateTime.parse((String) map.get("createdAt"), FORMATTER));
            } catch (Exception e) {
                user.setCreatedAt(ZonedDateTime.now());
            }
        }

        if (map.containsKey("updatedAt")) {
            try {
                user.setUpdatedAt(ZonedDateTime.parse((String) map.get("updatedAt"), FORMATTER));
            } catch (Exception e) {
                user.setUpdatedAt(ZonedDateTime.now());
            }
        }

        return user;
    }

    public static DateTimeFormatter getFormatter() {
        return FORMATTER;
    }
}
