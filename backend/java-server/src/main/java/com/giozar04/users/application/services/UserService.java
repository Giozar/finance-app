package com.giozar04.users.application.services;

import java.util.List;

import com.giozar04.users.domain.entities.User;
import com.giozar04.users.domain.interfaces.UserRepositoryInterface;

public class UserService implements UserRepositoryInterface {

    private final UserRepositoryInterface userRepository;

    public UserService(UserRepositoryInterface userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        return userRepository.createUser(user);
    }

    @Override
    public User getUserById(long id) {
        return userRepository.getUserById(id);
    }

    @Override
    public User updateUserById(long id, User user) {
        return userRepository.updateUserById(id, user);
    }

    @Override
    public void deleteUserById(long id) {
        userRepository.deleteUserById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }
}
