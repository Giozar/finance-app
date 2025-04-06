package com.giozar04.users.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.utils.SharedUtils;
import com.giozar04.users.domain.entities.User;

public class UserUtils {

    public static Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("password", user.getPassword());
        map.put("globalBalance", user.getGlobalBalance());

        if (user.getCreatedAt() != null) {
            map.put("createdAt", user.getCreatedAt().format(SharedUtils.getFormatter()));
        }
        if (user.getUpdatedAt() != null) {
            map.put("updatedAt", user.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static User mapToUser(Map<String, Object> map) {
        User user = new User();

        user.setId(SharedUtils.parseLong(map.get("id")));
        user.setName((String) map.getOrDefault("name", ""));
        user.setEmail((String) map.getOrDefault("email", ""));
        user.setPassword((String) map.getOrDefault("password", ""));
        user.setGlobalBalance(SharedUtils.parseDouble(map.get("globalBalance")));
        user.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        user.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));

        return user;
    }
}
