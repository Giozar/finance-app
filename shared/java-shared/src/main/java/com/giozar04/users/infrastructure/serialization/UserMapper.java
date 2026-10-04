package com.giozar04.users.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.infrastructure.serialization.ValueParser;
import com.giozar04.users.domain.entities.User;

public class UserMapper {

    public static Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("password", user.getPassword());
        map.put("globalBalance", user.getGlobalBalance());

        if (user.getCreatedAt() != null) {
            map.put("createdAt", user.getCreatedAt().format(ValueParser.getFormatter()));
        }
        if (user.getUpdatedAt() != null) {
            map.put("updatedAt", user.getUpdatedAt().format(ValueParser.getFormatter()));
        }

        return map;
    }

    public static User fromMap(Map<String, Object> map) {
        User user = new User();

        user.setId(ValueParser.parseLong(map.get("id")));
        user.setName((String) map.getOrDefault("name", ""));
        user.setEmail((String) map.getOrDefault("email", ""));
        user.setPassword((String) map.getOrDefault("password", ""));
        user.setGlobalBalance(ValueParser.parseDouble(map.get("globalBalance")));
        user.setCreatedAt(ValueParser.parseZonedDateTime(map.get("createdAt")));
        user.setUpdatedAt(ValueParser.parseZonedDateTime(map.get("updatedAt")));

        return user;
    }
}
