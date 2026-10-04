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
import com.giozar04.cardTransactionDetails.application.services.CardTransactionDetailService;
import com.giozar04.cardTransactionDetails.infrastructure.handlers.CardTransactionDetailHandlers;
import com.giozar04.cardTransactionDetails.infrastructure.repositories.CardTransactionDetailRepositoryMySQL;
import com.giozar04.cards.application.services.CardService;
import com.giozar04.cards.domain.interfaces.CardRepositoryInterface;
import com.giozar04.cards.infrastructure.handlers.CardHandlers;
import com.giozar04.cards.infrastructure.repositories.CardRepositoryMySQL;
import com.giozar04.categories.application.usecases.CategoryUseCase;
import com.giozar04.categories.application.ports.input.CategoryOperations;
import com.giozar04.categories.application.ports.output.CategoryRepository;
import com.giozar04.categories.infrastructure.transport.socket.CategoryHandlers;
import com.giozar04.categories.infrastructure.persistence.mysql.CategoryRepositoryMySQL;
import com.giozar04.configs.DatabaseConfig;
import com.giozar04.configs.ServerConfig;
import com.giozar04.databases.application.services.TransactionalExecutor;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.externalEntities.application.services.ExternalEntityService;
import com.giozar04.externalEntities.domain.interfaces.ExternalEntityRepositoryInterface;
import com.giozar04.externalEntities.infrastructure.handlers.ExternalEntityHandlers;
import com.giozar04.externalEntities.infrastructure.repositories.ExternalEntityRepositoryMySQL;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.exceptions.ServerOperationException;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.tags.application.usecases.TagUseCase;
import com.giozar04.tags.application.ports.input.TagOperations;
import com.giozar04.tags.application.ports.output.TagRepository;
import com.giozar04.tags.infrastructure.transport.socket.TagHandlers;
import com.giozar04.tags.infrastructure.persistence.mysql.TagRepositoryMySQL;
import com.giozar04.transactionTags.domain.interfaces.TransactionTagRepositoryInterface;
import com.giozar04.transactionTags.infrastructure.repositories.TransactionTagRepositoryMySQL;
import com.giozar04.transactions.application.normalizers.TransactionNormalizer;
import com.giozar04.transactions.application.services.TransactionService;
import com.giozar04.transactions.application.validation.TransactionRules;
import com.giozar04.transactions.application.validation.TransactionValidator;
import com.giozar04.transactions.application.validation.ValidationContextFactory;
import com.giozar04.transactions.domain.interfaces.TransactionRepositoryInterface;
import com.giozar04.transactions.infrastructure.handlers.TransactionHandlers;
import com.giozar04.transactions.infrastructure.repositories.TransactionRepositoryMySQL;
import com.giozar04.users.application.usecases.UserUseCase;
import com.giozar04.users.application.ports.input.UserOperations;
import com.giozar04.users.application.ports.output.UserRepository;
import com.giozar04.users.infrastructure.transport.socket.UserHandlers;
import com.giozar04.users.infrastructure.persistence.mysql.UserRepositoryMySQL;
import com.giozar04.accountCashbackSettings.application.services.AccountCashbackSettingService;
import com.giozar04.accountReconciliations.application.services.AccountReconciliationService;
import com.giozar04.accountReconciliations.domain.interfaces.AccountReconciliationRepositoryInterface;
import com.giozar04.accountReconciliations.infrastructure.handlers.AccountReconciliationHandlers;
import com.giozar04.accountReconciliations.infrastructure.repositories.AccountReconciliationRepositoryMySQL;
import com.giozar04.accountCashbackSettings.domain.interfaces.AccountCashbackSettingRepositoryInterface;
import com.giozar04.accountCashbackSettings.infrastructure.handlers.AccountCashbackSettingHandlers;
import com.giozar04.accountCashbackSettings.infrastructure.repositories.AccountCashbackSettingRepositoryMySQL;
import com.giozar04.walletCardLinks.application.services.WalletCardLinkService;
import com.giozar04.walletCardLinks.domain.interfaces.WalletCardLinkRepositoryInterface;
import com.giozar04.walletCardLinks.infrastructure.handlers.WalletCardLinkHandlers;
import com.giozar04.walletCardLinks.infrastructure.repositories.WalletCardLinkRepositoryMySQL;
import com.giozar04.walletTransactionDetails.application.services.WalletTransactionDetailService;
import com.giozar04.walletTransactionDetails.infrastructure.handlers.WalletTransactionDetailHandlers;
import com.giozar04.walletTransactionDetails.infrastructure.repositories.WalletTransactionDetailRepositoryMySQL;

public class ApplicationInitializer {
    private final ConsoleLogger logger = ConsoleLogger.getInstance();

    public void start() {
        logger.info("Iniciando aplicación...");

        DatabaseConfig databaseConfig = new DatabaseConfig();
        ServerConfig serverConfig = new ServerConfig();

        DatabaseInitializer dbInitializer = new DatabaseInitializer(databaseConfig, logger);
        DatabaseConnectionInterface dbConnection = dbInitializer.initialize();

        // Inicializar repositorios y servicios de usuarios
        UserRepository userRepository =
                new UserRepositoryMySQL(dbConnection);
        UserOperations userService = new UserUseCase(userRepository);

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
        CategoryRepository categoryRepository =
                new CategoryRepositoryMySQL(dbConnection);
        CategoryOperations categoryService = new CategoryUseCase(categoryRepository);

        // Inicializar repositorios y servicios de etiquetas
        TagRepository tagRepository =
                new TagRepositoryMySQL(dbConnection);
        TagOperations tagService = new TagUseCase(tagRepository);

        // Inicializar repositorios y servicios de entidades externas
        ExternalEntityRepositoryInterface externalEntityRepository =
                new ExternalEntityRepositoryMySQL(dbConnection);
        ExternalEntityService externalEntityService = new ExternalEntityService(externalEntityRepository);

        // Inicializar repositorios y servicios de detalles de transacciones con tarjeta
        // (la misma instancia MySQL sirve al CRUD y, como escritor, a la unidad de trabajo de transactions)
        CardTransactionDetailRepositoryMySQL cardTransactionDetailRepository =
                new CardTransactionDetailRepositoryMySQL(dbConnection);
        CardTransactionDetailService cardTransactionDetailService = new CardTransactionDetailService(cardTransactionDetailRepository);

        // Inicializar repositorios y servicios de detalles de transacciones de wallet
        WalletTransactionDetailRepositoryMySQL walletTransactionDetailRepository =
                new WalletTransactionDetailRepositoryMySQL(dbConnection);
        WalletTransactionDetailService walletTransactionDetailService = new WalletTransactionDetailService(walletTransactionDetailRepository);

        // Inicializar repositorios y servicios de vínculos wallet-tarjeta
        WalletCardLinkRepositoryInterface walletCardLinkRepository =
                new WalletCardLinkRepositoryMySQL(dbConnection);
        WalletCardLinkService walletCardLinkService = new WalletCardLinkService(walletCardLinkRepository);

        // Inicializar repositorios y servicios de transacciones (agregado: transacción + detalle + tags)
        TransactionalExecutor transactionalExecutor = new TransactionalExecutor(dbConnection);
        TransactionTagRepositoryInterface transactionTagRepository = new TransactionTagRepositoryMySQL();
        TransactionRepositoryInterface transactionRepository =
                new TransactionRepositoryMySQL(dbConnection, transactionalExecutor,
                        cardTransactionDetailRepository, walletTransactionDetailRepository, transactionTagRepository);
        ValidationContextFactory transactionValidationContextFactory = new ValidationContextFactory(
                accountRepository, cardRepository, walletCardLinkRepository,
                categoryRepository, externalEntityRepository, tagRepository);
        TransactionService transactionService = new TransactionService(
                transactionRepository,
                transactionValidationContextFactory,
                new TransactionNormalizer(),
                new TransactionValidator(TransactionRules.defaultRules()));

        // Inicializar repositorios y servicios de configuraciones de cashback
        AccountCashbackSettingRepositoryInterface accountCashbackSettingRepository =
                new AccountCashbackSettingRepositoryMySQL(dbConnection);
        AccountCashbackSettingService accountCashbackSettingService =
                new AccountCashbackSettingService(accountCashbackSettingRepository);

        // Inicializar repositorios y servicios de reconciliación de cuentas
        AccountReconciliationRepositoryInterface accountReconciliationRepository =
                new AccountReconciliationRepositoryMySQL(dbConnection);
        AccountReconciliationService accountReconciliationService =
                new AccountReconciliationService(accountReconciliationRepository);

        // Se registran todos los servicios
        List<ServerRegisterHandlers> featureServices = List.of(
                new UserHandlers(userService),
                new BankClientHandlers(bankClientService),
                new AccountHandlers(accountService),
                new CardHandlers(cardService),
                new CategoryHandlers(categoryService),
                new TagHandlers(tagService),
                new ExternalEntityHandlers(externalEntityService),
                new TransactionHandlers(transactionService),
                new CardTransactionDetailHandlers(cardTransactionDetailService),
                new WalletTransactionDetailHandlers(walletTransactionDetailService),
                new WalletCardLinkHandlers(walletCardLinkService),
                new AccountCashbackSettingHandlers(accountCashbackSettingService),
                new AccountReconciliationHandlers(accountReconciliationService)
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
