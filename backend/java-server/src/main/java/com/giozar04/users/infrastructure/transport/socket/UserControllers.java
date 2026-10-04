package com.giozar04.users.infrastructure.transport.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.messages.infrastructure.transport.Message;
import com.giozar04.servers.infrastructure.transport.socket.MessageHandler;
import com.giozar04.servers.infrastructure.transport.socket.ClientConnection;
import com.giozar04.users.application.ports.input.UserOperations;
import com.giozar04.users.infrastructure.serialization.UserMapper;
import com.giozar04.users.domain.entities.User;

public class UserControllers {

    private static final ConsoleLogger LOGGER = ConsoleLogger.getInstance();

    public static final class UserMessageTypes {
        public static final String CREATE_USER = "CREATE_USER";
        public static final String GET_USER = "GET_USER";
        public static final String UPDATE_USER = "UPDATE_USER";
        public static final String DELETE_USER = "DELETE_USER";
        public static final String GET_ALL_USERS = "GET_ALL_USERS";
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler createUserController(UserOperations userService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de creación de usuario");

            Map<String, Object> userData = (Map<String, Object>) message.getData("user");
            if (userData == null) {
                return Message.createErrorMessage(UserMessageTypes.CREATE_USER,
                        "Datos de usuario no proporcionados");
            }

            User user = UserMapper.fromMap(userData);
            User created = userService.createUser(user);

            Message response = Message.createSuccessMessage(UserMessageTypes.CREATE_USER,
                    "Usuario creado exitosamente");
            response.addData("user", UserMapper.toMap(created));
            return response;
        };
    }

    public static MessageHandler getUserController(UserOperations userService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de usuario");

            Object rawId = message.getData("id");
            Long id = parseId(rawId);
            if (id == null) {
                return Message.createErrorMessage(UserMessageTypes.GET_USER, "ID de usuario inválido o no proporcionado");
            }

            User user = userService.getUserById(id);
            Message response = Message.createSuccessMessage(UserMessageTypes.GET_USER,
                    "Usuario obtenido exitosamente");
            response.addData("user", UserMapper.toMap(user));
            return response;
        };
    }

    @SuppressWarnings("unchecked")
    public static MessageHandler updateUserController(UserOperations userService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de actualización de usuario");

            Object rawId = message.getData("id");
            Long id = parseId(rawId);
            if (id == null) {
                return Message.createErrorMessage(UserMessageTypes.UPDATE_USER, "ID de usuario inválido o no proporcionado");
            }

            Map<String, Object> userData = (Map<String, Object>) message.getData("user");
            if (userData == null) {
                return Message.createErrorMessage(UserMessageTypes.UPDATE_USER, "Datos de usuario no proporcionados");
            }

            User user = UserMapper.fromMap(userData);
            User updated = userService.updateUserById(id, user);

            Message response = Message.createSuccessMessage(UserMessageTypes.UPDATE_USER,
                    "Usuario actualizado exitosamente");
            response.addData("user", UserMapper.toMap(updated));
            return response;
        };
    }

    public static MessageHandler deleteUserController(UserOperations userService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de eliminación de usuario");

            Object rawId = message.getData("id");
            Long id = parseId(rawId);
            if (id == null) {
                return Message.createErrorMessage(UserMessageTypes.DELETE_USER, "ID de usuario inválido o no proporcionado");
            }

            userService.deleteUserById(id);

            return Message.createSuccessMessage(UserMessageTypes.DELETE_USER,
                    "Usuario eliminado exitosamente");
        };
    }

    public static MessageHandler getAllUsersController(UserOperations userService) {
        return (ClientConnection clientConnection, Message message) -> {
            LOGGER.info("Procesando solicitud de obtención de todos los usuarios");

            List<User> users = userService.getAllUsers();
            List<Map<String, Object>> userList = new ArrayList<>();

            for (User user : users) {
                userList.add(UserMapper.toMap(user));
            }

            Message response = Message.createSuccessMessage(UserMessageTypes.GET_ALL_USERS,
                    "Usuarios obtenidos exitosamente");
            response.addData("users", userList);
            response.addData("count", users.size());

            return response;
        };
    }

    private static Long parseId(Object rawId) {
        if (rawId instanceof Long aLong) return aLong;
        if (rawId instanceof String string) {
            try {
                return Long.valueOf(string);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
