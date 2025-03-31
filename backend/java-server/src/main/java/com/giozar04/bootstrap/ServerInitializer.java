package com.giozar04.bootstrap;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.giozar04.configs.ServerConfig;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.exceptions.ServerOperationException;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.shared.logging.CustomLogger;

public class ServerInitializer {
        private final ServerConfig serverConfig;
        private final CustomLogger logger;
        private final ExecutorService threadPool;
    
        public ServerInitializer(ServerConfig serverConfig, CustomLogger logger) {
            this.serverConfig = serverConfig;
            this.logger = logger;
            this.threadPool = Executors.newCachedThreadPool();
        }
    
        public ServerService initialize(List<ServerRegisterHandlers> featureRegistrars)
                throws ServerOperationException, IOException {
    
            ServerService server = ServerService.getInstance(
                    serverConfig.getHost(),
                    serverConfig.getPort(),
                    threadPool,
                    logger
            );
    
            for (ServerRegisterHandlers registrar : featureRegistrars) {
                registrar.register(server);
            }
    
            logger.info("Todos los manejadores registrados correctamente");
    
            return server;
        }
    }
    