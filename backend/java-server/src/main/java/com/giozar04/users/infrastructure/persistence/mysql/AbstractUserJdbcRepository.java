package com.giozar04.users.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.application.ports.output.UserRepository;
import com.giozar04.users.domain.policies.UserPolicy;

public abstract class AbstractUserJdbcRepository implements UserRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger =  ConsoleLogger.getInstance();

    protected AbstractUserJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection,
            "La conexión a la base de datos no puede ser nula");
    }

    protected void validateUser(User user) {
        UserPolicy.validateUser(user);
    }

    protected void validateId(long id) {
        UserPolicy.validateId(id);
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
