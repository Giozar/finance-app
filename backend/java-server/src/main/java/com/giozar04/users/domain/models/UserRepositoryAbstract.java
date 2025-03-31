package com.giozar04.users.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.shared.logging.CustomLogger;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.domain.interfaces.UserRepositoryInterface;

public abstract class UserRepositoryAbstract implements UserRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger;

    protected UserRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, 
            "La conexión a la base de datos no puede ser nula");
        this.logger = new CustomLogger();
    }

    protected void validateUser(User user) {
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

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

    @Override
    public abstract User createUser(User user);

    @Override
    public abstract User getUserById(long id);

    @Override
    public abstract User updateUserById(long id, User user);

    @Override
    public abstract void deleteUserById(long id);

    @Override
    public abstract List<User> getAllUsers();
}
