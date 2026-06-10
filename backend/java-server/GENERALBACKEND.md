# The client project has the following structure:
├── .
├── database
│   ├── schemas.sql
├── GENERAL.md
├── backend
│   ├── java-server
│   │   ├── pom.xml
│   │   ├── .gitignore
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