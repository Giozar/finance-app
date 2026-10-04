package com.giozar04.bootstrap;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.giozar04.configs.ServerConfig;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.servers.infrastructure.transport.socket.ServerService;
import com.giozar04.servers.infrastructure.transport.socket.ServerOperationException;
import com.giozar04.servers.infrastructure.transport.socket.ServerRegisterHandlers;

public class ServerInitializer {
        private final ServerConfig serverConfig;
        private final ConsoleLogger logger = ConsoleLogger.getInstance();
        private final ExecutorService threadPool;
    
        public ServerInitializer(ServerConfig serverConfig) {
            this.serverConfig = serverConfig;
            this.threadPool = Executors.newCachedThreadPool();
        }
    
        public ServerService initialize(List<ServerRegisterHandlers> featureRegistrars)
                throws ServerOperationException, IOException {
    
            ServerService server = ServerService.getInstance(
                    serverConfig.getHost(),
                    serverConfig.getPort(),
                    threadPool
            );
    
            for (ServerRegisterHandlers registrar : featureRegistrars) {
                registrar.register(server);
            }
    
            logger.info("Todos los manejadores registrados correctamente");
    
            return server;
        }
    }
    