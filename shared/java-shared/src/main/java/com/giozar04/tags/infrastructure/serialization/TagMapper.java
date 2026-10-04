package com.giozar04.tags.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.shared.infrastructure.serialization.ValueParser;
import com.giozar04.tags.domain.entities.Tag;

public class TagMapper {

    public static Map<String, Object> toMap(Tag tag) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tag.getId());
        map.put("userId", tag.getUserId());
        map.put("name", tag.getName());
        map.put("color", tag.getColor());

        if (tag.getCreatedAt() != null) {
            map.put("createdAt", tag.getCreatedAt().format(ValueParser.getFormatter()));
        }

        if (tag.getUpdatedAt() != null) {
            map.put("updatedAt", tag.getUpdatedAt().format(ValueParser.getFormatter()));
        }

        return map;
    }

    public static Tag fromMap(Map<String, Object> map) {
        Tag tag = new Tag();
        tag.setId(ValueParser.parseLong(map.get("id")));
        tag.setUserId(ValueParser.parseLong(map.get("userId")));
        tag.setName((String) map.get("name"));
        tag.setColor((String) map.get("color"));
        tag.setCreatedAt(ValueParser.parseZonedDateTime(map.get("createdAt")));
        tag.setUpdatedAt(ValueParser.parseZonedDateTime(map.get("updatedAt")));
        return tag;
    }
}
