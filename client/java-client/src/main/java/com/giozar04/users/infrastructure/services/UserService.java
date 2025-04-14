package com.giozar04.users.infrastructure.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.CustomLogger;
import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.serverConnection.application.services.ServerConnectionService;
import com.giozar04.serverConnection.application.validators.ServerResponseValidator;
import com.giozar04.users.application.utils.UserUtils;
import com.giozar04.users.domain.entities.User;
import com.giozar04.users.domain.exceptions.UserExceptions;

public class UserService {

    private final ServerConnectionService serverConnectionService;
    private static final CustomLogger logger = CustomLogger.getInstance();
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

    @SuppressWarnings("unchecked")
    public User createUser(User user) throws ClientOperationException {
        Message message = new Message();
        message.setType("CREATE_USER");
        message.addData("user", UserUtils.userToMap(user));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("CREATE_USER");
            ServerResponseValidator.validateResponse(response);
            logger.info("Usuario creado exitosamente: " + response);
            return UserUtils.mapToUser((Map<String, Object>) response.getData("user"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserExceptions.UserCreationException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public User updateUserById(Long userId, User user) throws ClientOperationException {
        Message message = new Message();
        message.setType("UPDATE_USER");
        message.addData("id", userId);
        message.addData("user", UserUtils.userToMap(user));

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("UPDATE_USER");
            ServerResponseValidator.validateResponse(response);
            logger.info("Usuario actualizado correctamente: " + response);
            return UserUtils.mapToUser((Map<String, Object>) response.getData("user"));
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
            ServerResponseValidator.validateResponse(response);
            logger.info("Usuario eliminado exitosamente: " + response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserExceptions.UserDeletionException("Error al esperar la respuesta del servidor", e);
        }
    }

    @SuppressWarnings("unchecked")
    public User getUserById(Long userId) throws ClientOperationException {
        Message message = new Message();
        message.setType("GET_USER");
        message.addData("id", userId);

        serverConnectionService.sendMessage(message);
        try {
            Message response = serverConnectionService.waitForMessage("GET_USER");
            ServerResponseValidator.validateResponse(response);
            logger.info("Usuario obtenido: " + response);
            return UserUtils.mapToUser((Map<String, Object>) response.getData("user"));
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
            ServerResponseValidator.validateResponse(response);
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
                logger.info("Usuarios obtenidos correctamente. Total: " + users.size());
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
