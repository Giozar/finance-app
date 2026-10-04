# The shared project has the following structure: 
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
│   │   │   │   │   │   │   │   │   │   ├── TransactionStatus.java
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