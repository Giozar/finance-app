package com.giozar04.externalEntities.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;
import com.giozar04.shared.infrastructure.serialization.ValueParser;

public class ExternalEntityMapper {

    public static Map<String, Object> toMap(ExternalEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("userId", entity.getUserId());
        map.put("name", entity.getName());
        map.put("type", entity.getType() != null ? entity.getType().getValue() : null);
        map.put("contact", entity.getContact());

        if (entity.getCreatedAt() != null) {
            map.put("createdAt", entity.getCreatedAt().format(ValueParser.getFormatter()));
        }

        if (entity.getUpdatedAt() != null) {
            map.put("updatedAt", entity.getUpdatedAt().format(ValueParser.getFormatter()));
        }

        return map;
    }

    public static ExternalEntity fromMap(Map<String, Object> map) {
        ExternalEntity entity = new ExternalEntity();
        entity.setId(ValueParser.parseLong(map.get("id")));
        entity.setUserId(ValueParser.parseLong(map.get("userId")));
        entity.setName((String) map.get("name"));

        Object typeObj = map.get("type");
        if (typeObj != null) {
            entity.setType(ExternalEntityTypes.fromValue(typeObj.toString()));
        }

        entity.setContact((String) map.get("contact"));
        entity.setCreatedAt(ValueParser.parseZonedDateTime(map.get("createdAt")));
        entity.setUpdatedAt(ValueParser.parseZonedDateTime(map.get("updatedAt")));
        return entity;
    }
}
