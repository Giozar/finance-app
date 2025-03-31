package com.giozar04.users.infrastructure.handlers;

import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.users.application.services.UserService;
import com.giozar04.users.infrastructure.controllers.UserControllers;

public class UserHandlers implements ServerRegisterHandlers {
    private final UserService transactionService;

    public UserHandlers(UserService transactionService) {
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
