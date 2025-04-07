package com.giozar04.users.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.users.application.utils.UserUtils;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.domain.exceptions.UserExceptions;

public class UserService {

    private final ServerConnectionService serverConnectionService;
    private static UserService instance;

    private UserService(ServerConnectionService serverConnectionService) {
        this.serverConnectionService = serverConnectionService;
    }

    public static UserService connectService(ServerConnectionService serverConnectionService) {
        if (instance == null) {
            instance = new UserService(serverConnectionService);
        }
        return instance;
    }

    public static UserService getInstance() {
        return instance;
    }

    public void createUser(User user) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_USER");
        message.addData("user", UserUtils.userToMap(user));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_USER");
            System.out.println("[CLIENT] Usuario creado: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserExceptions.UserCreationException("Error al esperar la respuesta del servidor", e);
        }
    }

    public void updateUserById(Long userId, User user) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_USER");
        message.addData("id", userId);
        message.addData("user", UserUtils.userToMap(user));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_USER");
            System.out.println("[CLIENT] Usuario actualizado: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserExceptions.UserUpdateException("Error al esperar la respuesta del servidor", e);
        }
    }

    public void deleteUserById(Long userId) throws ClientOperationException {
        Message message = new Message();
        message.setType("DELETE_USER");
        message.addData("id", userId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("DELETE_USER");
            System.out.println("[CLIENT] Usuario eliminado: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserExceptions.UserDeletionException("Error al esperar la respuesta del servidor", e);
        }
    }

    public void getUserById(Long userId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_USER");
        message.addData("id", userId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_USER");
            System.out.println("[CLIENT] Usuario recibido: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserExceptions.UserRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_ALL_USERS");

        serverConnectionService.sendMessage(message);

        try {
            Message response = serverConnectionService.waitForMessage("GET_ALL_USERS");
            Object raw = response.getData("users");

            if (raw == null) {
                throw new UserExceptions.UserRetrievalException(
                        "El servidor respondió sin incluir la lista de usuarios", null
                );
            }

            if (raw instanceof List<?> rawList) {
                List<User> users = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        users.add(UserUtils.mapToUser((Map<String, Object>) map));
                    }
                }
                return users;
            } else {
                throw new UserExceptions.UserParsingException(
                        "Formato inesperado: " + raw.getClass().getName(), null
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserExceptions.UserRetrievalException("Error al esperar la respuesta del servidor", e);
        }
    }
}
