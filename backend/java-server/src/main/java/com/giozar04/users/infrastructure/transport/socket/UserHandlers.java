package com.giozar04.users.infrastructure.transport.socket;

import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;
import com.giozar04.users.application.ports.input.UserOperations;
import com.giozar04.users.infrastructure.transport.socket.UserControllers;

public class UserHandlers implements ServerRegisterHandlers {
    private final UserOperations transactionService;

    public UserHandlers(UserOperations transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void register(ServerService server) {
        server.registerHandler(
            UserControllers.UserMessageTypes.CREATE_USER,
            UserControllers.createUserController(transactionService)
        );
        server.registerHandler(
            UserControllers.UserMessageTypes.GET_USER,
            UserControllers.getUserController(transactionService)
        );
        server.registerHandler(
            UserControllers.UserMessageTypes.UPDATE_USER,
            UserControllers.updateUserController(transactionService)
        );
        server.registerHandler(
            UserControllers.UserMessageTypes.DELETE_USER,
            UserControllers.deleteUserController(transactionService)
        );
        server.registerHandler(
            UserControllers.UserMessageTypes.GET_ALL_USERS,
            UserControllers.getAllUsersController(transactionService)
        );
    }
}
