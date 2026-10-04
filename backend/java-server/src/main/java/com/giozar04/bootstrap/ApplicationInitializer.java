package com.giozar04.bootstrap;

import java.io.IOException;
import java.util.List;

import com.giozar04.accounts.application.usecases.AccountUseCase;
import com.giozar04.accounts.application.ports.input.AccountOperations;
import com.giozar04.accounts.application.ports.output.AccountRepository;
import com.giozar04.accounts.infrastructure.transport.socket.AccountHandlers;
import com.giozar04.accounts.infrastructure.persistence.mysql.AccountRepositoryMySQL;
import com.giozar04.bankClients.application.usecases.BankClientUseCase;
import com.giozar04.bankClients.application.ports.input.BankClientOperations;
import com.giozar04.bankClients.application.ports.output.BankClientRepository;
import com.giozar04.bankClients.infrastructure.transport.socket.BankClientHandlers;
import com.giozar04.bankClients.infrastructure.persistence.mysql.BankClientRepositoryMySQL;
import com.giozar04.cardTransactionDetails.application.usecases.CardTransactionDetailUseCase;
import com.giozar04.cardTransactionDetails.application.ports.input.CardTransactionDetailOperations;
import com.giozar04.cardTransactionDetails.infrastructure.transport.socket.CardTransactionDetailHandlers;
import com.giozar04.cardTransactionDetails.infrastructure.persistence.mysql.CardTransactionDetailRepositoryMySQL;
import com.giozar04.cards.application.usecases.CardUseCase;
import com.giozar04.cards.application.ports.input.CardOperations;
import com.giozar04.cards.application.ports.output.CardRepository;
import com.giozar04.cards.infrastructure.transport.socket.CardHandlers;
import com.giozar04.cards.infrastructure.persistence.mysql.CardRepositoryMySQL;
import com.giozar04.categories.application.usecases.CategoryUseCase;
import com.giozar04.categories.application.ports.input.CategoryOperations;
import com.giozar04.categories.application.ports.output.CategoryRepository;
import com.giozar04.categories.infrastructure.transport.socket.CategoryHandlers;
import com.giozar04.categories.infrastructure.persistence.mysql.CategoryRepositoryMySQL;
import com.giozar04.configs.DatabaseConfig;
import com.giozar04.configs.ServerConfig;
import com.giozar04.databases.application.services.TransactionalExecutor;
import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.externalEntities.application.usecases.ExternalEntityUseCase;
import com.giozar04.externalEntities.application.ports.input.ExternalEntityOperations;
import com.giozar04.externalEntities.application.ports.output.ExternalEntityRepository;
import com.giozar04.externalEntities.infrastructure.transport.socket.ExternalEntityHandlers;
import com.giozar04.externalEntities.infrastructure.persistence.mysql.ExternalEntityRepositoryMySQL;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.servers.application.services.ServerService;
import com.giozar04.servers.domain.exceptions.ServerOperationException;
import com.giozar04.servers.domain.interfaces.ServerRegisterHandlers;
import com.giozar04.tags.application.usecases.TagUseCase;
import com.giozar04.tags.application.ports.input.TagOperations;
import com.giozar04.tags.application.ports.output.TagRepository;
import com.giozar04.tags.infrastructure.transport.socket.TagHandlers;
import com.giozar04.tags.infrastructure.persistence.mysql.TagRepositoryMySQL;
import com.giozar04.transactionTags.infrastructure.persistence.mysql.TransactionTagJdbcOperations;
import com.giozar04.transactionTags.infrastructure.persistence.mysql.TransactionTagRepositoryMySQL;
import com.giozar04.transactions.application.normalizers.TransactionNormalizer;
import com.giozar04.transactions.application.usecases.TransactionUseCase;
import com.giozar04.transactions.application.ports.input.TransactionOperations;
import com.giozar04.transactions.application.validation.TransactionRules;
import com.giozar04.transactions.application.validation.TransactionValidator;
import com.giozar04.transactions.application.validation.ValidationContextFactory;
import com.giozar04.transactions.application.ports.output.TransactionRepository;
import com.giozar04.transactions.infrastructure.transport.socket.TransactionHandlers;
import com.giozar04.transactions.infrastructure.persistence.mysql.TransactionRepositoryMySQL;
import com.giozar04.users.application.usecases.UserUseCase;
import com.giozar04.users.application.ports.input.UserOperations;
import com.giozar04.users.application.ports.output.UserRepository;
import com.giozar04.users.infrastructure.transport.socket.UserHandlers;
import com.giozar04.users.infrastructure.persistence.mysql.UserRepositoryMySQL;
import com.giozar04.accountCashbackSettings.application.usecases.AccountCashbackSettingUseCase;
import com.giozar04.accountCashbackSettings.application.ports.input.AccountCashbackSettingOperations;
import com.giozar04.accountReconciliations.application.usecases.AccountReconciliationUseCase;
import com.giozar04.accountReconciliations.application.ports.input.AccountReconciliationOperations;
import com.giozar04.accountReconciliations.application.ports.output.AccountReconciliationRepository;
import com.giozar04.accountReconciliations.infrastructure.transport.socket.AccountReconciliationHandlers;
import com.giozar04.accountReconciliations.infrastructure.persistence.mysql.AccountReconciliationRepositoryMySQL;
import com.giozar04.accountCashbackSettings.application.ports.output.AccountCashbackSettingRepository;
import com.giozar04.accountCashbackSettings.infrastructure.transport.socket.AccountCashbackSettingHandlers;
import com.giozar04.accountCashbackSettings.infrastructure.persistence.mysql.AccountCashbackSettingRepositoryMySQL;
import com.giozar04.walletCardLinks.application.usecases.WalletCardLinkUseCase;
import com.giozar04.walletCardLinks.application.ports.input.WalletCardLinkOperations;
import com.giozar04.walletCardLinks.application.ports.output.WalletCardLinkRepository;
import com.giozar04.walletCardLinks.infrastructure.transport.socket.WalletCardLinkHandlers;
import com.giozar04.walletCardLinks.infrastructure.persistence.mysql.WalletCardLinkRepositoryMySQL;
import com.giozar04.walletTransactionDetails.application.usecases.WalletTransactionDetailUseCase;
import com.giozar04.walletTransactionDetails.application.ports.input.WalletTransactionDetailOperations;
import com.giozar04.walletTransactionDetails.infrastructure.transport.socket.WalletTransactionDetailHandlers;
import com.giozar04.walletTransactionDetails.infrastructure.persistence.mysql.WalletTransactionDetailRepositoryMySQL;

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
        BankClientRepository bankClientRepository =
                new BankClientRepositoryMySQL(dbConnection);
        BankClientOperations bankClientService = new BankClientUseCase(bankClientRepository);

        // Inicializar repositorios y servicios de cuentas
        AccountRepository accountRepository =
                new AccountRepositoryMySQL(dbConnection);
        AccountOperations accountService = new AccountUseCase(accountRepository);

        // Inicializar repositorios y servicios de tarjetas
        CardRepository cardRepository =
                new CardRepositoryMySQL(dbConnection);
        CardOperations cardService = new CardUseCase(cardRepository);

        // Inicializar repositorios y servicios de categorías
        CategoryRepository categoryRepository =
                new CategoryRepositoryMySQL(dbConnection);
        CategoryOperations categoryService = new CategoryUseCase(categoryRepository);

        // Inicializar repositorios y servicios de etiquetas
        TagRepository tagRepository =
                new TagRepositoryMySQL(dbConnection);
        TagOperations tagService = new TagUseCase(tagRepository);

        // Inicializar repositorios y servicios de entidades externas
        ExternalEntityRepository externalEntityRepository =
                new ExternalEntityRepositoryMySQL(dbConnection);
        ExternalEntityOperations externalEntityService = new ExternalEntityUseCase(externalEntityRepository);

        // Inicializar repositorios y servicios de detalles de transacciones con tarjeta
        // (la misma instancia MySQL sirve al CRUD y, como escritor, a la unidad de trabajo de transactions)
        CardTransactionDetailRepositoryMySQL cardTransactionDetailRepository =
                new CardTransactionDetailRepositoryMySQL(dbConnection);
        CardTransactionDetailOperations cardTransactionDetailService = new CardTransactionDetailUseCase(cardTransactionDetailRepository);

        // Inicializar repositorios y servicios de detalles de transacciones de wallet
        WalletTransactionDetailRepositoryMySQL walletTransactionDetailRepository =
                new WalletTransactionDetailRepositoryMySQL(dbConnection);
        WalletTransactionDetailOperations walletTransactionDetailService = new WalletTransactionDetailUseCase(walletTransactionDetailRepository);

        // Inicializar repositorios y servicios de vínculos wallet-tarjeta
        WalletCardLinkRepository walletCardLinkRepository =
                new WalletCardLinkRepositoryMySQL(dbConnection);
        WalletCardLinkOperations walletCardLinkService = new WalletCardLinkUseCase(walletCardLinkRepository);

        // Inicializar repositorios y servicios de transacciones (agregado: transacción + detalle + tags)
        TransactionalExecutor transactionalExecutor = new TransactionalExecutor(dbConnection);
        TransactionTagJdbcOperations transactionTagRepository = new TransactionTagRepositoryMySQL();
        TransactionRepository transactionRepository =
                new TransactionRepositoryMySQL(dbConnection, transactionalExecutor,
                        cardTransactionDetailRepository, walletTransactionDetailRepository, transactionTagRepository);
        ValidationContextFactory transactionValidationContextFactory = new ValidationContextFactory(
                accountRepository, cardRepository, walletCardLinkRepository,
                categoryRepository, externalEntityRepository, tagRepository);
        TransactionOperations transactionService = new TransactionUseCase(
                transactionRepository,
                transactionValidationContextFactory,
                new TransactionNormalizer(),
                new TransactionValidator(TransactionRules.defaultRules()));

        // Inicializar repositorios y servicios de configuraciones de cashback
        AccountCashbackSettingRepository accountCashbackSettingRepository =
                new AccountCashbackSettingRepositoryMySQL(dbConnection);
        AccountCashbackSettingUseCase accountCashbackSettingService =
                new AccountCashbackSettingUseCase(accountCashbackSettingRepository);

        // Inicializar repositorios y servicios de reconciliación de cuentas
        AccountReconciliationRepository accountReconciliationRepository =
                new AccountReconciliationRepositoryMySQL(dbConnection);
        AccountReconciliationUseCase accountReconciliationService =
                new AccountReconciliationUseCase(accountReconciliationRepository);

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
