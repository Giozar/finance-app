package com.giozar04.bootstrap;

import java.io.IOException;
import java.util.List;

import com.giozar04.accounts.application.services.AccountService;
import com.giozar04.accounts.domain.interfaces.AccountRepositoryInterface;
import com.giozar04.accounts.infrastructure.handlers.AccountHandlers;
import com.giozar04.accounts.infrastructure.repositories.AccountRepositoryMySQL;
import com.giozar04.bankClients.application.services.BankClientService;
import com.giozar04.bankClients.domain.interfaces.BankClientRepositoryInterface;
import com.giozar04.bankClients.infrastructure.handlers.BankClientHandlers;
import com.giozar04.bankClients.infrastructure.repositories.BankClientRepositoryMySQL;
import com.giozar04.cards.application.services.CardService;
import com.giozar04.cards.domain.interfaces.CardRepositoryInterface;
import com.giozar04.cards.infrastructure.handlers.CardHandlers;
import com.giozar04.cards.infrastructure.repositories.CardRepositoryMySQL;
import com.giozar04.categories.application.services.CategoryService;
import com.giozar04.categories.domain.interfaces.CategoryRepositoryInterface;
import com.giozar04.categories.infrastructure.handlers.CategoryHandlers;
import com.giozar04.categories.infrastructure.repositories.CategoryRepositoryMySQL;
import com.giozar04.configs.DatabaseConfig;
import com.giozar04.configs.ServerConfig;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.exceptions.ServerOperationException;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.tags.application.services.TagService;
import com.giozar04.tags.domain.interfaces.TagRepositoryInterface;
import com.giozar04.tags.infrastructure.handlers.TagHandlers;
import com.giozar04.tags.infrastructure.repositories.TagRepositoryMySQL;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.domain.interfaces.TransactionRepositoryInterface;
import com.giozar04.transactions.infrastructure.handlers.TransactionHandlers;
import com.giozar04.transactions.infrastructure.repositories.TransactionRepositoryMySQL;
import com.giozar04.users.application.services.UserService;
import com.giozar04.users.domain.interfaces.UserRepositoryInterface;
import com.giozar04.users.infrastructure.handlers.UserHandlers;
import com.giozar04.users.infrastructure.repositories.UserRepositoryMySQL;

public class ApplicationInitializer {
    private final CustomLogger logger = CustomLogger.getInstance();

    public void start() {
        logger.info("Iniciando aplicación...");

        DatabaseConfig databaseConfig = new DatabaseConfig();
        ServerConfig serverConfig = new ServerConfig();

        DatabaseInitializer dbInitializer = new DatabaseInitializer(databaseConfig, logger);
        DatabaseConnectionInterface dbConnection = dbInitializer.initialize();

        // Inicializar repositorios y servicios de usuarios
        UserRepositoryInterface userRepository =
                new UserRepositoryMySQL(dbConnection);
        UserService userService = new UserService(userRepository);

        // Inicializar repositorios y servicios de clientes de bancos
        BankClientRepositoryInterface bankClientRepository =
                new BankClientRepositoryMySQL(dbConnection);
        BankClientService bankClientService = new BankClientService(bankClientRepository);

        // Inicializar repositorios y servicios de cuentas
        AccountRepositoryInterface accountRepository =
                new AccountRepositoryMySQL(dbConnection);
        AccountService accountService = new AccountService(accountRepository);

        // Inicializar repositorios y servicios de tarjetas
        CardRepositoryInterface cardRepository =
                new CardRepositoryMySQL(dbConnection);
        CardService cardService = new CardService(cardRepository);

        // Inicializar repositorios y servicios de categorías
        CategoryRepositoryInterface categoryRepository =
                new CategoryRepositoryMySQL(dbConnection);
        CategoryService categoryService = new CategoryService(categoryRepository);

        // Inicializar repositorios y servicios de etiquetas
        TagRepositoryInterface tagRepository =
                new TagRepositoryMySQL(dbConnection);
        TagService tagService = new TagService(tagRepository);

        // Inicializar repositorios y servicios de transacciones
        TransactionRepositoryInterface transactionRepository =
                new TransactionRepositoryMySQL(dbConnection);
        TransactionService transactionService =
                new TransactionService(transactionRepository);

        
        // Se registran todos los servicios
        List<ServerRegisterHandlers> featureServices = List.of(
                new UserHandlers(userService),
                new BankClientHandlers(bankClientService),
                new AccountHandlers(accountService),
                new CardHandlers(cardService),
                new CategoryHandlers(categoryService),
                new TagHandlers(tagService),
                new TransactionHandlers(transactionService)
        );

        logger.info("Servicios inicializados correctamente.");

        ServerInitializer serverInitializer = new ServerInitializer(serverConfig);
        try {

            ServerService server = serverInitializer.initialize(featureServices);
            server.startServer();
            logger.info("Servidor iniciado correctamente en " + serverConfig.getHost() + ":" + serverConfig.getPort());
            
            // Mantener el servidor en ejecución
            keepServerRunning();
        } catch (ServerOperationException | IOException e) {
            logger.error("Error al iniciar la aplicación", e);
            System.exit(1);
        }
    }

    private void keepServerRunning() {
        logger.info("Servidor en ejecución. Presiona Ctrl+C para detener.");
        final Object lock = new Object();
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            logger.info("Aplicación interrumpida. Finalizando...");
            Thread.currentThread().interrupt();
        }
    }
}
