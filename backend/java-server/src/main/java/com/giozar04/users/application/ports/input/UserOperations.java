package com.giozar04.users.application.ports.input;

import java.util.List;

import com.giozar04.users.domain.entities.User;

public interface UserOperations {
    User createUser(User user);
    User getUserById(long id);
    User updateUserById(long id, User user);
    void deleteUserById(long id);
    List<User> getAllUsers();
}
