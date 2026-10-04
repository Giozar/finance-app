package com.giozar04.users.application.ports.input;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.users.domain.entities.User;

public interface UserOperations {
    User createUser(User user) throws ClientOperationException;
    User updateUserById(Long userId, User user) throws ClientOperationException;
    void deleteUserById(Long userId) throws ClientOperationException;
    User getUserById(Long userId) throws ClientOperationException;
    List<User> getAllUsers() throws ClientOperationException;
}
