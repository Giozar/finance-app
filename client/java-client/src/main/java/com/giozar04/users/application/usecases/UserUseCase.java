package com.giozar04.users.application.usecases;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.application.ports.input.UserOperations;
import com.giozar04.users.application.ports.output.UserGateway;

public final class UserUseCase implements UserOperations {
    private final UserGateway gateway;

    public UserUseCase(UserGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public User createUser(User user) throws ClientOperationException {
        return gateway.createUser(user);
    }

    @Override
    public User updateUserById(Long userId, User user) throws ClientOperationException {
        return gateway.updateUserById(userId, user);
    }

    @Override
    public void deleteUserById(Long userId) throws ClientOperationException {
        gateway.deleteUserById(userId);
    }

    @Override
    public User getUserById(Long userId) throws ClientOperationException {
        return gateway.getUserById(userId);
    }

    @Override
    public List<User> getAllUsers() throws ClientOperationException {
        return gateway.getAllUsers();
    }
}
