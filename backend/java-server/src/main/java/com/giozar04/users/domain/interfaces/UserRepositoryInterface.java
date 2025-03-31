package com.giozar04.users.domain.interfaces;

import java.util.List;

import com.giozar04.users.domain.entities.User;

public interface UserRepositoryInterface {
    User createUser(User user);
    User getUserById(long id);
    User updateUserById(long id, User user);
    void deleteUserById(long id);
    List<User> getAllUsers();
}
