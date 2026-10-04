package com.giozar04.users.domain.policies;

import java.util.List;
import java.util.Objects;
import com.giozar04.users.domain.entities.User;

public final class UserPolicy {
    private UserPolicy() {}

    public static void validateUser(User user) {
        Objects.requireNonNull(user, "El usuario no puede ser nulo");

        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
    }

    public static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }
}
