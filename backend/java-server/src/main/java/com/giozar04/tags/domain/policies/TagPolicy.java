package com.giozar04.tags.domain.policies;

import java.util.Objects;

import com.giozar04.tags.domain.entities.Tag;

/** Reglas de entrada de etiquetas, independientes del transporte y de MySQL. */
public final class TagPolicy {
    private TagPolicy() {}

    public static void validate(Tag tag) {
        Objects.requireNonNull(tag, "La etiqueta no puede ser nula");
        if (tag.getName() == null || tag.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío");
        }
        if (tag.getColor() == null || tag.getColor().isBlank()) {
            throw new IllegalArgumentException("El color de la etiqueta no puede estar vacío");
        }
        if (tag.getUserId() <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido para la etiqueta");
        }
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
