package com.giozar04.categories.domain.policies;

import java.util.List;
import java.util.Objects;
import com.giozar04.categories.domain.entities.Category;
import com.giozar04.categories.domain.enums.CategoryTypes;

public final class CategoryPolicy {
    private CategoryPolicy() {}

    public static void validateCategory(Category category) {
        Objects.requireNonNull(category, "La categoría no puede ser nula");

        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }

        if (category.getType() == null) {
            throw new IllegalArgumentException("El tipo de categoría es obligatorio");
        }

        try {
            CategoryTypes.valueOf(category.getType().name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de categoría no válido: " + category.getType());
        }

        if (category.getUserId() <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido para la categoría");
        }

        if (category.getIcon() == null || category.getIcon().isBlank()) {
            throw new IllegalArgumentException("El ícono de la categoría no puede estar vacío");
        }
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
