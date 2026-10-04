# The project has the following structure:
├── .
├── database
│   ├── migrations
│   │   ├── 2026-10-03_reallocation.sql
│   │   ├── 2026-10-03_schema_consistency.sql
│   │   ├── 2026-10-03_transaction_integrity.sql
│   │   ├── 2026-10-04_account_reconciliation.sql
│   │   ├── 2026-10-05_transactions_redesign.sql
│   ├── schemas.sql
├── GENERAL.md
├── backend
│   ├── java-server
│   │   ├── pom.xml
│   │   ├── .gitignore
│   │   ├── GENERALBACKEND.md
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── resources
│   │   │   │   │   ├── config.properties
│   │   │   │   │   ├── config.example.properties
│   │   │   │   ├── java
│   │   │   │   │   ├── com
│   │   │   │   │   │   ├── giozar04
│   │   │   │   │   │   │   ├── databases
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── DatabaseConnectionMySQL.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── DatabaseConnectionAbstract.java
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── DatabaseExceptions.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── DatabaseConnectionInterface.java
│   │   │   │   │   │   │   ├── walletCardLinks
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── WalletCardLinkTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── wallet_card_links.sql
│   │   │   │   │   │   │   ├── walletTransactionDetails
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── wallet_transaction_details.sql
│   │   │   │   │   │   │   ├── bootstrap
│   │   │   │   │   │   │   │   ├── ServerInitializer.java
│   │   │   │   │   │   │   │   ├── ApplicationInitializer.java
│   │   │   │   │   │   │   │   ├── DatabaseInitializer.java
│   │   │   │   │   │   │   ├── cardTransactionDetails
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── card_transaction_details.sql
│   │   │   │   │   │   │   ├── accountCashbackSettings
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── account_cashback_settings.sql
│   │   │   │   │   │   │   ├── cards
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── CardTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── CardService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── CardRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── CardControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── CardHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── CardRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── CardRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── card.sql
│   │   │   │   │   │   │   ├── bankClients
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── BankClientTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── BankClientService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── BankClientRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── BankClientControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── BankClientHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── BankClientRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── BankClientRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── bankClients.sql
│   │   │   │   │   │   │   ├── tags
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── TagTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── TagService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── TagRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── TagControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── TagHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── TagRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── TagRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── tag.sql
│   │   │   │   │   │   │   ├── transactions
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── TransactionTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── TransactionService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── TransactionRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── TransactionControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── TransactionHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── TransactionRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── TransactionRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── transactions.sql
│   │   │   │   │   │   │   ├── accounts
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── AccountTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── AccountService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── AccountRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── AccountControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── AccountHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── AccountRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── AccountRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── account.sql
│   │   │   │   │   │   │   ├── users
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── TestUserApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── UserService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── UserRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── UserControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── UserHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── UserRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── UserRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── users.sql
│   │   │   │   │   │   │   ├── configs
│   │   │   │   │   │   │   │   ├── DatabaseConfig.java
│   │   │   │   │   │   │   │   ├── ServerConfig.java
│   │   │   │   │   │   │   │   ├── AppConfig.java
│   │   │   │   │   │   │   ├── Main.java
│   │   │   │   │   │   │   ├── backend-explanation.md
│   │   │   │   │   │   │   ├── externalEntities
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── ExternalEntityTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── externalEntity.sql
│   │   │   │   │   │   │   ├── categories
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── CategoryTestApp.java
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── CategoryService.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── repositories
│   │   │   │   │   │   │   │   │   │   ├── CategoryRepositoryMySQL.java
│   │   │   │   │   │   │   │   │   ├── controllers
│   │   │   │   │   │   │   │   │   │   ├── CategoryControllers.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── CategoryHandlers.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── CategoryRepositoryAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── CategoryRepositoryInterface.java
│   │   │   │   │   │   │   │   ├── sql
│   │   │   │   │   │   │   │   │   ├── categories.sql
│   │   │   │   │   │   │   ├── servers
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── ServerService.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── ClientConnection.java
│   │   │   │   │   │   │   │   │   │   ├── ServerAbstract.java
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── ServerOperationException.java
│   │   │   │   │   │   │   │   │   ├── handlers
│   │   │   │   │   │   │   │   │   │   ├── MessageHandler.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── ServerRegisterHandlers.java
│   │   │   │   │   │   │   │   │   │   ├── ServerInterface.java
├── shared
│   ├── java-shared
│   │   ├── pom.xml
│   │   ├── README.md
│   │   ├── .gitignore
│   │   ├── GENERALSHARED.md
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   ├── com
│   │   │   │   │   │   ├── giozar04
│   │   │   │   │   │   │   ├── walletCardLinks
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLink.java
│   │   │   │   │   │   │   ├── messages
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── Message.java
│   │   │   │   │   │   │   ├── walletTransactionDetails
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── enums
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionSourceType.java
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetail.java
│   │   │   │   │   │   │   ├── card
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── CardUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── enums
│   │   │   │   │   │   │   │   │   │   ├── CardTypes.java
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── CardExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── Card.java
│   │   │   │   │   │   │   ├── cardTransactionDetails
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetail.java
│   │   │   │   │   │   │   ├── bankClient
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── BankClientUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── BankClientExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── BankClient.java
│   │   │   │   │   │   │   ├── accountCashbackSettings
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSetting.java
│   │   │   │   │   │   │   ├── shared
│   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   ├── SharedUtils.java
│   │   │   │   │   │   │   ├── tags
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── TagUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── TagExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── Tag.java
│   │   │   │   │   │   │   ├── transactions
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── TransactionUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── enums
│   │   │   │   │   │   │   │   │   │   ├── PaymentMethod.java
│   │   │   │   │   │   │   │   │   │   ├── OperationTypes.java
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── TransactionExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── Transaction.java
│   │   │   │   │   │   │   ├── json
│   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   ├── JsonUtils.java
│   │   │   │   │   │   │   ├── accounts
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── AccountUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── enums
│   │   │   │   │   │   │   │   │   │   ├── AccountTypes.java
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── AccountExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── Account.java
│   │   │   │   │   │   │   ├── shared-explanation.md
│   │   │   │   │   │   │   ├── users
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── UserUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── UserExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── User.java
│   │   │   │   │   │   │   ├── Main.java
│   │   │   │   │   │   │   ├── externalEntities
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── enums
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityTypes.java
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntity.java
│   │   │   │   │   │   │   ├── categories
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   │   ├── CategoryUtils.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── enums
│   │   │   │   │   │   │   │   │   │   ├── CategoryTypes.java
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── CategoryExceptions.java
│   │   │   │   │   │   │   │   │   ├── entities
│   │   │   │   │   │   │   │   │   │   ├── Category.java
│   │   │   │   │   │   │   ├── logging
│   │   │   │   │   │   │   │   ├── CustomLogger.java
├── README.md
├── .gitignore
├── client
│   ├── java-client
│   │   ├── GENERALCLIENT.md
│   │   ├── pom.xml
│   │   ├── .gitignore
│   │   ├── src
│   │   │   ├── test
│   │   │   │   ├── java
│   │   │   │   │   ├── TestTable.java
│   │   │   ├── main
│   │   │   │   ├── resources
│   │   │   │   │   ├── config.properties
│   │   │   │   │   ├── config.example.properties
│   │   │   │   ├── java
│   │   │   │   │   ├── com
│   │   │   │   │   │   ├── giozar04
│   │   │   │   │   │   │   ├── walletCardLinks
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── WalletCardLinkService.java
│   │   │   │   │   │   │   ├── client-explanation.md
│   │   │   │   │   │   │   ├── walletTransactionDetails
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── WalletTransactionDetailService.java
│   │   │   │   │   │   │   ├── bootstrap
│   │   │   │   │   │   │   │   ├── ApplicationInitializer.java
│   │   │   │   │   │   │   ├── cardTransactionDetails
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── CardTransactionDetailService.java
│   │   │   │   │   │   │   ├── accountCashbackSettings
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── AccountCashbackSettingService.java
│   │   │   │   │   │   │   ├── cards
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── CardCreationTest.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── CardService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   │   ├── CardFormPanel.java
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── CardsView.java
│   │   │   │   │   │   │   │   │   │   ├── CreateCardView.java
│   │   │   │   │   │   │   ├── shared
│   │   │   │   │   │   │   │   ├── utils
│   │   │   │   │   │   │   │   │   ├── DialogUtil.java
│   │   │   │   │   │   │   │   │   ├── FormValidatorUtils.java
│   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   ├── MainContentPanel.java
│   │   │   │   │   │   │   │   │   ├── forms
│   │   │   │   │   │   │   │   │   │   ├── FormDateField.java
│   │   │   │   │   │   │   │   │   │   ├── FormTextArea.java
│   │   │   │   │   │   │   │   │   │   ├── FormComboBox.java
│   │   │   │   │   │   │   │   │   │   ├── FormField.java
│   │   │   │   │   │   │   │   │   │   ├── PercentageField.java
│   │   │   │   │   │   │   │   │   │   ├── FormLabel.java
│   │   │   │   │   │   │   │   │   │   ├── ColorPickerField.java
│   │   │   │   │   │   │   │   │   ├── HeaderPanel.java
│   │   │   │   │   │   │   │   │   ├── table
│   │   │   │   │   │   │   │   │   │   ├── OptionsCellEditor.java
│   │   │   │   │   │   │   │   │   │   ├── OptionsCellRenderer.java
│   │   │   │   │   │   │   │   │   │   ├── GenericTablePanel.java
│   │   │   │   │   │   │   │   │   │   ├── GenericTableModel.java
│   │   │   │   │   │   │   │   │   │   ├── PopupMenuActionHandler.java
│   │   │   │   │   │   │   │   │   │   ├── ColumnDefinition.java
│   │   │   │   │   │   │   │   │   ├── CreditUsagePanel.java
│   │   │   │   │   │   │   │   │   ├── SidebarPanel.java
│   │   │   │   │   │   │   │   │   ├── DatePickerComponent.java
│   │   │   │   │   │   │   │   ├── layouts
│   │   │   │   │   │   │   │   │   ├── AppLayout.java
│   │   │   │   │   │   │   ├── bankClients
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── BankClientFunctionalTest.java
│   │   │   │   │   │   │   │   │   ├── BankClientGuiFunctionalTest.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── BankClientService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   │   ├── BankNameCellRenderer.java
│   │   │   │   │   │   │   │   │   │   ├── BankClientFormPanel.java
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── CreateBankClientView.java
│   │   │   │   │   │   │   │   │   │   ├── BankClientsView.java
│   │   │   │   │   │   │   ├── tags
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── TagService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   │   ├── TagFormPanel.java
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── CreateTagView.java
│   │   │   │   │   │   │   │   │   │   ├── TagsView.java
│   │   │   │   │   │   │   ├── dashboard
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── MainDashboardView.java
│   │   │   │   │   │   │   ├── transactions
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── TransactionService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   │   ├── TransactionTypeCellRenderer.java
│   │   │   │   │   │   │   │   │   │   ├── PaymentMethodCellRenderer.java
│   │   │   │   │   │   │   │   │   │   ├── TransactionFormPanel.java
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── CreateTransactionView.java
│   │   │   │   │   │   │   │   │   │   ├── TransactionsView.java
│   │   │   │   │   │   │   ├── accounts
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── AccountFunctionalTest.java
│   │   │   │   │   │   │   │   │   ├── AccountGuiFunctionalTest.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── AccountService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   │   ├── subpanels
│   │   │   │   │   │   │   │   │   │   │   ├── BankDetailsSubPanel.java
│   │   │   │   │   │   │   │   │   │   │   ├── WalletCardLinksPanel.java
│   │   │   │   │   │   │   │   │   │   │   ├── CashbackSettingsPanel.java
│   │   │   │   │   │   │   │   │   │   │   ├── CreditDetailsSubPanel.java
│   │   │   │   │   │   │   │   │   │   │   ├── SavingsDetailsSubPanel.java
│   │   │   │   │   │   │   │   │   │   │   ├── InvestmentDetailsSubPanel.java
│   │   │   │   │   │   │   │   │   │   ├── AccountFormPanel.java
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── CreateAccountView.java
│   │   │   │   │   │   │   │   │   │   ├── AccountDetailView.java
│   │   │   │   │   │   │   │   │   │   ├── detail
│   │   │   │   │   │   │   │   │   │   │   ├── BenefitAccountDetailView.java
│   │   │   │   │   │   │   │   │   │   │   ├── SavingsAccountDetailView.java
│   │   │   │   │   │   │   │   │   │   │   ├── WalletAccountDetailView.java
│   │   │   │   │   │   │   │   │   │   │   ├── DebitAccountDetailView.java
│   │   │   │   │   │   │   │   │   │   │   ├── BaseAccountDetailView.java
│   │   │   │   │   │   │   │   │   │   │   ├── InvestmentAccountDetailView.java
│   │   │   │   │   │   │   │   │   │   │   ├── CreditAccountDetailView.java
│   │   │   │   │   │   │   │   │   │   │   ├── CashAccountDetailView.java
│   │   │   │   │   │   │   │   │   │   ├── AccountsView.java
│   │   │   │   │   │   │   ├── serverConnection
│   │   │   │   │   │   │   │   ├── application
│   │   │   │   │   │   │   │   │   ├── exceptions
│   │   │   │   │   │   │   │   │   │   ├── ClientOperationException.java
│   │   │   │   │   │   │   │   │   ├── validators
│   │   │   │   │   │   │   │   │   │   ├── ServerResponseValidator.java
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── ServerConnectionService.java
│   │   │   │   │   │   │   │   ├── domain
│   │   │   │   │   │   │   │   │   ├── models
│   │   │   │   │   │   │   │   │   │   ├── ServerConnectionConfig.java
│   │   │   │   │   │   │   │   │   │   ├── ServerConnectionAbstract.java
│   │   │   │   │   │   │   │   │   ├── interfaces
│   │   │   │   │   │   │   │   │   │   ├── ServerConnectionInterface.java
│   │   │   │   │   │   │   ├── users
│   │   │   │   │   │   │   │   ├── test
│   │   │   │   │   │   │   │   │   ├── UserGuiFunctionalTest.java
│   │   │   │   │   │   │   │   │   ├── UserFunctionalTest.java
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── UserService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   │   ├── UserFormPanel.java
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── CreateUserView.java
│   │   │   │   │   │   │   │   │   │   ├── UsersView.java
│   │   │   │   │   │   │   ├── configs
│   │   │   │   │   │   │   │   ├── ServerConnectionConfig.java
│   │   │   │   │   │   │   │   ├── AppConfig.java
│   │   │   │   │   │   │   ├── Main.java
│   │   │   │   │   │   │   ├── externalEntities
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntityFormPanel.java
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── CreateExternalEntityView.java
│   │   │   │   │   │   │   │   │   │   ├── ExternalEntitiesView.java
│   │   │   │   │   │   │   ├── categories
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── CategoryService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── components
│   │   │   │   │   │   │   │   │   │   ├── CategoryFormPanel.java
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── CategoriesView.java
│   │   │   │   │   │   │   │   │   │   ├── CreateCategoryView.java