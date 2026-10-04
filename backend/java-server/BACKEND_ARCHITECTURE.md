# Arquitectura de backend

Mapa de la estructura actual del backend.

[Arquitectura común](../../ARCHITECTURE.md) · [Agente](../../.claude/agents/finance-app-expert-backend.md)

## Árbol del módulo

```text
└── java-server
    ├── pom.xml
    ├── .gitignore
    ├── BACKEND_ARCHITECTURE.md
    └── src
        ├── test
        │   └── java
        │       └── com
        │           └── giozar04
        │               ├── tags
        │               │   └── TagUseCaseProbe.java
        │               └── transactions
        │                   └── TransactionUseCaseProbe.java
        └── main
            ├── resources
            │   ├── config.example.properties
            │   └── config.properties
            └── java
                └── com
                    └── giozar04
                        ├── accountCashbackSettings
                        │   ├── test
                        │   │   └── AccountCashbackSettingTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── AccountCashbackSettingOperations.java
                        │   │   │   └── output
                        │   │   │       └── AccountCashbackSettingRepository.java
                        │   │   └── usecases
                        │   │       └── AccountCashbackSettingUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── AccountCashbackSettingPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractAccountCashbackSettingJdbcRepository.java
                        │   │   │       └── AccountCashbackSettingRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── AccountCashbackSettingControllers.java
                        │   │           └── AccountCashbackSettingHandlers.java
                        │   └── sql
                        │       └── account_cashback_settings.sql
                        ├── accountReconciliations
                        │   ├── test
                        │   │   └── AccountReconciliationTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── AccountReconciliationOperations.java
                        │   │   │   └── output
                        │   │   │       └── AccountReconciliationRepository.java
                        │   │   └── usecases
                        │   │       └── AccountReconciliationUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── AccountReconciliationPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractAccountReconciliationJdbcRepository.java
                        │   │   │       └── AccountReconciliationRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── AccountReconciliationControllers.java
                        │   │           └── AccountReconciliationHandlers.java
                        │   └── sql
                        │       └── account_reconciliation.sql
                        ├── accounts
                        │   ├── test
                        │   │   └── AccountTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── AccountOperations.java
                        │   │   │   └── output
                        │   │   │       └── AccountRepository.java
                        │   │   └── usecases
                        │   │       └── AccountUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── AccountPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractAccountJdbcRepository.java
                        │   │   │       └── AccountRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── AccountControllers.java
                        │   │           └── AccountHandlers.java
                        │   └── sql
                        │       └── account.sql
                        ├── BACKEND_FEATURE_GUIDE.md
                        ├── bankClients
                        │   ├── test
                        │   │   └── BankClientTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── BankClientOperations.java
                        │   │   │   └── output
                        │   │   │       └── BankClientRepository.java
                        │   │   └── usecases
                        │   │       └── BankClientUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── BankClientPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractBankClientJdbcRepository.java
                        │   │   │       └── BankClientRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── BankClientControllers.java
                        │   │           └── BankClientHandlers.java
                        │   └── sql
                        │       └── bankClients.sql
                        ├── bootstrap
                        │   ├── ApplicationInitializer.java
                        │   ├── DatabaseInitializer.java
                        │   └── ServerInitializer.java
                        ├── cards
                        │   ├── test
                        │   │   └── CardTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── CardOperations.java
                        │   │   │   └── output
                        │   │   │       └── CardRepository.java
                        │   │   └── usecases
                        │   │       └── CardUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── CardPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractCardJdbcRepository.java
                        │   │   │       └── CardRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── CardControllers.java
                        │   │           └── CardHandlers.java
                        │   └── sql
                        │       └── card.sql
                        ├── cardTransactionDetails
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── CardTransactionDetailOperations.java
                        │   │   │   └── output
                        │   │   │       └── CardTransactionDetailRepository.java
                        │   │   └── usecases
                        │   │       └── CardTransactionDetailUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── CardTransactionDetailPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractCardTransactionDetailJdbcRepository.java
                        │   │   │       ├── CardTransactionDetailJdbcOperations.java
                        │   │   │       └── CardTransactionDetailRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── CardTransactionDetailControllers.java
                        │   │           └── CardTransactionDetailHandlers.java
                        │   └── sql
                        │       └── card_transaction_details.sql
                        ├── categories
                        │   ├── test
                        │   │   └── CategoryTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── CategoryOperations.java
                        │   │   │   └── output
                        │   │   │       └── CategoryRepository.java
                        │   │   └── usecases
                        │   │       └── CategoryUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── CategoryPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractCategoryJdbcRepository.java
                        │   │   │       └── CategoryRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── CategoryControllers.java
                        │   │           └── CategoryHandlers.java
                        │   └── sql
                        │       └── categories.sql
                        ├── configs
                        │   ├── AppConfig.java
                        │   ├── DatabaseConfig.java
                        │   └── ServerConfig.java
                        ├── databases
                        │   └── infrastructure
                        │       └── persistence
                        │           └── mysql
                        │               ├── DatabaseConnectionAbstract.java
                        │               ├── DatabaseConnectionInterface.java
                        │               ├── DatabaseConnectionMySQL.java
                        │               ├── DatabaseExceptions.java
                        │               ├── SqlWork.java
                        │               └── TransactionalExecutor.java
                        ├── externalEntities
                        │   ├── test
                        │   │   └── ExternalEntityTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── ExternalEntityOperations.java
                        │   │   │   └── output
                        │   │   │       └── ExternalEntityRepository.java
                        │   │   └── usecases
                        │   │       └── ExternalEntityUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── ExternalEntityPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractExternalEntityJdbcRepository.java
                        │   │   │       └── ExternalEntityRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── ExternalEntityControllers.java
                        │   │           └── ExternalEntityHandlers.java
                        │   └── sql
                        │       └── externalEntity.sql
                        ├── Main.java
                        ├── servers
                        │   └── infrastructure
                        │       └── transport
                        │           └── socket
                        │               ├── ClientConnection.java
                        │               ├── MessageHandler.java
                        │               ├── ServerAbstract.java
                        │               ├── ServerInterface.java
                        │               ├── ServerOperationException.java
                        │               ├── ServerRegisterHandlers.java
                        │               └── ServerService.java
                        ├── tags
                        │   ├── test
                        │   │   └── TagTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── TagOperations.java
                        │   │   │   └── output
                        │   │   │       └── TagRepository.java
                        │   │   └── usecases
                        │   │       └── TagUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── TagPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractTagJdbcRepository.java
                        │   │   │       └── TagRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── TagControllers.java
                        │   │           └── TagHandlers.java
                        │   └── sql
                        │       └── tag.sql
                        ├── transactions
                        │   ├── test
                        │   │   └── TransactionTestApp.java
                        │   ├── application
                        │   │   ├── normalizers
                        │   │   │   └── TransactionNormalizer.java
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── TransactionOperations.java
                        │   │   │   └── output
                        │   │   │       └── TransactionRepository.java
                        │   │   ├── usecases
                        │   │   │   └── TransactionUseCase.java
                        │   │   └── validation
                        │   │       ├── EnumDispatchRule.java
                        │   │       ├── rules
                        │   │       │   ├── CardPaymentRule.java
                        │   │       │   ├── CommonFieldsRule.java
                        │   │       │   ├── ExpenseRule.java
                        │   │       │   ├── IncomeRule.java
                        │   │       │   ├── InternalPaymentRule.java
                        │   │       │   ├── NoDetailPaymentRule.java
                        │   │       │   ├── ReallocationRule.java
                        │   │       │   ├── TransactionRuleSupport.java
                        │   │       │   └── WalletPaymentRule.java
                        │   │       ├── TransactionRule.java
                        │   │       ├── TransactionRules.java
                        │   │       ├── TransactionValidator.java
                        │   │       ├── ValidationContext.java
                        │   │       └── ValidationContextFactory.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── TransactionPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractTransactionJdbcRepository.java
                        │   │   │       └── TransactionRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── TransactionControllers.java
                        │   │           └── TransactionHandlers.java
                        │   └── sql
                        │       └── transactions.sql
                        ├── transactionTags
                        │   ├── infrastructure
                        │   │   └── persistence
                        │   │       └── mysql
                        │   │           ├── TransactionTagJdbcOperations.java
                        │   │           └── TransactionTagRepositoryMySQL.java
                        │   └── sql
                        │       └── transaction_tags.sql
                        ├── users
                        │   ├── test
                        │   │   └── TestUserApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── UserOperations.java
                        │   │   │   └── output
                        │   │   │       └── UserRepository.java
                        │   │   └── usecases
                        │   │       └── UserUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── UserPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractUserJdbcRepository.java
                        │   │   │       └── UserRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── UserControllers.java
                        │   │           └── UserHandlers.java
                        │   └── sql
                        │       └── users.sql
                        ├── walletCardLinks
                        │   ├── test
                        │   │   └── WalletCardLinkTestApp.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── WalletCardLinkOperations.java
                        │   │   │   └── output
                        │   │   │       └── WalletCardLinkRepository.java
                        │   │   └── usecases
                        │   │       └── WalletCardLinkUseCase.java
                        │   ├── domain
                        │   │   └── policies
                        │   │       └── WalletCardLinkPolicy.java
                        │   ├── infrastructure
                        │   │   ├── persistence
                        │   │   │   └── mysql
                        │   │   │       ├── AbstractWalletCardLinkJdbcRepository.java
                        │   │   │       └── WalletCardLinkRepositoryMySQL.java
                        │   │   └── transport
                        │   │       └── socket
                        │   │           ├── WalletCardLinkControllers.java
                        │   │           └── WalletCardLinkHandlers.java
                        │   └── sql
                        │       └── wallet_card_links.sql
                        └── walletTransactionDetails
                            ├── application
                            │   ├── ports
                            │   │   ├── input
                            │   │   │   └── WalletTransactionDetailOperations.java
                            │   │   └── output
                            │   │       └── WalletTransactionDetailRepository.java
                            │   └── usecases
                            │       └── WalletTransactionDetailUseCase.java
                            ├── domain
                            │   └── policies
                            │       └── WalletTransactionDetailPolicy.java
                            ├── infrastructure
                            │   ├── persistence
                            │   │   └── mysql
                            │   │       ├── AbstractWalletTransactionDetailJdbcRepository.java
                            │   │       ├── WalletTransactionDetailJdbcOperations.java
                            │   │       └── WalletTransactionDetailRepositoryMySQL.java
                            │   └── transport
                            │       └── socket
                            │           ├── WalletTransactionDetailControllers.java
                            │           └── WalletTransactionDetailHandlers.java
                            └── sql
                                └── wallet_transaction_details.sql
```
