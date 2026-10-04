package com.giozar04.categories.infrastructure.serialization;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;
import com.giozar04.shared.infrastructure.serialization.ValueParser;

public class CategoryMapper {

    public static Map<String, Object> toMap(Category category) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("userId", category.getUserId());
        map.put("name", category.getName());
        map.put("type", category.getType() != null ? category.getType().getValue() : null);
        map.put("icon", category.getIcon());

        if (category.getCreatedAt() != null) {
            map.put("createdAt", category.getCreatedAt().format(ValueParser.getFormatter()));
        }

        if (category.getUpdatedAt() != null) {
            map.put("updatedAt", category.getUpdatedAt().format(ValueParser.getFormatter()));
        }

        return map;
    }

    public static Category fromMap(Map<String, Object> map) {
        Category category = new Category();
        category.setId(ValueParser.parseLong(map.get("id")));
        category.setUserId(ValueParser.parseLong(map.get("userId")));
        category.setName((String) map.get("name"));

        Object typeObj = map.get("type");
        if (typeObj != null) {
            category.setType(CategoryTypes.fromValue(typeObj.toString()));
        }

        category.setIcon((String) map.get("icon"));
        category.setCreatedAt(ValueParser.parseZonedDateTime(map.get("createdAt")));
        category.setUpdatedAt(ValueParser.parseZonedDateTime(map.get("updatedAt")));
        return category;
    }
}
