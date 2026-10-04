package com.giozar04.users.application.ports.output;

import java.util.List;

import com.giozar04.users.domain.entities.User;

public interface UserRepository {
    User createUser(User user);
    User getUserById(long id);
    User updateUserById(long id, User user);
    void deleteUserById(long id);
    List<User> getAllUsers();
}
