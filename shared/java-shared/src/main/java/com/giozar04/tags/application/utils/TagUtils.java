package com.giozar04.tags.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.utils.SharedUtils;
import com.giozar04.tags.domain.entities.Tag;

public class TagUtils {

    public static Map<String, Object> tagToMap(Tag tag) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tag.getId());
        map.put("userId", tag.getUserId());
        map.put("name", tag.getName());
        map.put("color", tag.getColor());

        if (tag.getCreatedAt() != null) {
            map.put("createdAt", tag.getCreatedAt().format(SharedUtils.getFormatter()));
        }

        if (tag.getUpdatedAt() != null) {
            map.put("updatedAt", tag.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static Tag mapToTag(Map<String, Object> map) {
        Tag tag = new Tag();
        tag.setId(SharedUtils.parseLong(map.get("id")));
        tag.setUserId(SharedUtils.parseLong(map.get("userId")));
        tag.setName((String) map.get("name"));
        tag.setColor((String) map.get("color"));
        tag.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        tag.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));
        return tag;
    }
}
