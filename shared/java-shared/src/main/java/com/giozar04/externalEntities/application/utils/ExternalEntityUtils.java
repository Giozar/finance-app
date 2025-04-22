package com.giozar04.externalEntities.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.externalEntities.domain.entities.ExternalEntity;
import com.giozar04.externalEntities.domain.enums.ExternalEntityTypes;
import com.giozar04.shared.utils.SharedUtils;

public class ExternalEntityUtils {

    public static Map<String, Object> externalEntityToMap(ExternalEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("name", entity.getName());
        map.put("type", entity.getType() != null ? entity.getType().getValue() : null);
        map.put("contact", entity.getContact());

        if (entity.getCreatedAt() != null) {
            map.put("createdAt", entity.getCreatedAt().format(SharedUtils.getFormatter()));
        }

        if (entity.getUpdatedAt() != null) {
            map.put("updatedAt", entity.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static ExternalEntity mapToExternalEntity(Map<String, Object> map) {
        ExternalEntity entity = new ExternalEntity();
        entity.setId(SharedUtils.parseLong(map.get("id")));
        entity.setName((String) map.get("name"));
        entity.setType(ExternalEntityTypes.fromValue((String) map.get("type")));
        entity.setContact((String) map.get("contact"));
        entity.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        entity.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));
        return entity;
    }
}
