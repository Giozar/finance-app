# Arquitectura de shared

Mapa de la estructura actual de shared.

[Arquitectura común](../../ARCHITECTURE.md) · [Guía](SHARED_GUIDE.md) · [Agente](../../.claude/agents/finance-app-expert-shared.md)

## Árbol del módulo

```text
└── java-shared
    ├── pom.xml
    ├── .gitignore
    ├── SHARED_ARCHITECTURE.md
    ├── SHARED_GUIDE.md
    └── src
        ├── test
        │   ├── resources
        │   │   └── contracts.json
        │   └── java
        │       └── com
        │           └── giozar04
        │               └── contracts
        │                   └── ContractProbe.java
        └── main
            └── java
                └── com
                    └── giozar04
                        ├── accountCashbackSettings
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── AccountCashbackSettingCreationException.java
                        │   │       ├── AccountCashbackSettingDeletionException.java
                        │   │       ├── AccountCashbackSettingNotFoundException.java
                        │   │       ├── AccountCashbackSettingRetrievalException.java
                        │   │       └── AccountCashbackSettingUpdateException.java
                        │   ├── domain
                        │   │   └── entities
                        │   │       └── AccountCashbackSetting.java
                        │   └── infrastructure
                        │       └── serialization
                        │           ├── AccountCashbackSettingMapper.java
                        │           └── AccountCashbackSettingParsingException.java
                        ├── accountReconciliations
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── AccountReconciliationAdjustmentException.java
                        │   │       └── AccountReconciliationRetrievalException.java
                        │   ├── domain
                        │   │   └── entities
                        │   │       └── AccountReconciliation.java
                        │   └── infrastructure
                        │       └── serialization
                        │           └── AccountReconciliationMapper.java
                        ├── accounts
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── AccountCreationException.java
                        │   │       ├── AccountDeletionException.java
                        │   │       ├── AccountNotFoundException.java
                        │   │       ├── AccountRetrievalException.java
                        │   │       └── AccountUpdateException.java
                        │   ├── domain
                        │   │   ├── entities
                        │   │   │   └── Account.java
                        │   │   └── enums
                        │   │       └── AccountTypes.java
                        │   └── infrastructure
                        │       └── serialization
                        │           ├── AccountMapper.java
                        │           └── AccountParsingException.java
                        ├── bankClient
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── BankClientCreationException.java
                        │   │       ├── BankClientDeletionException.java
                        │   │       ├── BankClientNotFoundException.java
                        │   │       ├── BankClientRetrievalException.java
                        │   │       └── BankClientUpdateException.java
                        │   ├── domain
                        │   │   ├── entities
                        │   │   │   └── BankClient.java
                        │   │   └── exceptions
                        │   │       └── BankClientValidationException.java
                        │   └── infrastructure
                        │       └── serialization
                        │           ├── BankClientMapper.java
                        │           └── BankClientParsingException.java
                        ├── card
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── CardCreationException.java
                        │   │       ├── CardDeletionException.java
                        │   │       ├── CardNotFoundException.java
                        │   │       ├── CardRetrievalException.java
                        │   │       └── CardUpdateException.java
                        │   ├── domain
                        │   │   ├── entities
                        │   │   │   └── Card.java
                        │   │   └── enums
                        │   │       └── CardTypes.java
                        │   └── infrastructure
                        │       └── serialization
                        │           ├── CardMapper.java
                        │           └── CardParsingException.java
                        ├── cardTransactionDetails
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── CardTransactionDetailCreationException.java
                        │   │       ├── CardTransactionDetailDeletionException.java
                        │   │       ├── CardTransactionDetailNotFoundException.java
                        │   │       ├── CardTransactionDetailRetrievalException.java
                        │   │       └── CardTransactionDetailUpdateException.java
                        │   ├── domain
                        │   │   └── entities
                        │   │       └── CardTransactionDetail.java
                        │   └── infrastructure
                        │       └── serialization
                        │           └── CardTransactionDetailMapper.java
                        ├── categories
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── CategoryCreationException.java
                        │   │       ├── CategoryDeletionException.java
                        │   │       ├── CategoryNotFoundException.java
                        │   │       ├── CategoryRetrievalException.java
                        │   │       └── CategoryUpdateException.java
                        │   ├── domain
                        │   │   ├── entities
                        │   │   │   └── Category.java
                        │   │   └── enums
                        │   │       └── CategoryTypes.java
                        │   └── infrastructure
                        │       └── serialization
                        │           └── CategoryMapper.java
                        ├── externalEntities
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── ExternalEntityCreationException.java
                        │   │       ├── ExternalEntityDeletionException.java
                        │   │       ├── ExternalEntityNotFoundException.java
                        │   │       ├── ExternalEntityRetrievalException.java
                        │   │       └── ExternalEntityUpdateException.java
                        │   ├── domain
                        │   │   ├── entities
                        │   │   │   └── ExternalEntity.java
                        │   │   └── enums
                        │   │       └── ExternalEntityTypes.java
                        │   └── infrastructure
                        │       └── serialization
                        │           └── ExternalEntityMapper.java
                        ├── logging
                        │   └── infrastructure
                        │       └── ConsoleLogger.java
                        ├── Main.java
                        ├── messages
                        │   └── infrastructure
                        │       ├── serialization
                        │       │   └── MessageJsonCodec.java
                        │       └── transport
                        │           └── Message.java
                        ├── shared
                        │   └── infrastructure
                        │       └── serialization
                        │           └── ValueParser.java
                        ├── tags
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── TagCreationException.java
                        │   │       ├── TagDeletionException.java
                        │   │       ├── TagNotFoundException.java
                        │   │       ├── TagRetrievalException.java
                        │   │       └── TagUpdateException.java
                        │   ├── domain
                        │   │   └── entities
                        │   │       └── Tag.java
                        │   └── infrastructure
                        │       └── serialization
                        │           └── TagMapper.java
                        ├── transactions
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── TransactionCreationException.java
                        │   │       ├── TransactionDeletionException.java
                        │   │       ├── TransactionNotFoundException.java
                        │   │       ├── TransactionRetrievalException.java
                        │   │       └── TransactionUpdateException.java
                        │   ├── domain
                        │   │   ├── entities
                        │   │   │   └── Transaction.java
                        │   │   ├── enums
                        │   │   │   ├── OperationTypes.java
                        │   │   │   ├── PaymentMethod.java
                        │   │   │   └── TransactionStatus.java
                        │   │   └── exceptions
                        │   │       └── TransactionValidationException.java
                        │   └── infrastructure
                        │       └── serialization
                        │           ├── TransactionMapper.java
                        │           └── TransactionParsingException.java
                        ├── users
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── UserAuthenticationException.java
                        │   │       ├── UserCreationException.java
                        │   │       ├── UserDeletionException.java
                        │   │       ├── UserNotFoundException.java
                        │   │       ├── UserRetrievalException.java
                        │   │       └── UserUpdateException.java
                        │   ├── domain
                        │   │   └── entities
                        │   │       └── User.java
                        │   └── infrastructure
                        │       └── serialization
                        │           ├── UserMapper.java
                        │           └── UserParsingException.java
                        ├── walletCardLinks
                        │   ├── application
                        │   │   └── exceptions
                        │   │       ├── WalletCardLinkCreationException.java
                        │   │       ├── WalletCardLinkDeletionException.java
                        │   │       ├── WalletCardLinkNotFoundException.java
                        │   │       ├── WalletCardLinkRetrievalException.java
                        │   │       └── WalletCardLinkUpdateException.java
                        │   ├── domain
                        │   │   └── entities
                        │   │       └── WalletCardLink.java
                        │   └── infrastructure
                        │       └── serialization
                        │           └── WalletCardLinkMapper.java
                        └── walletTransactionDetails
                            ├── application
                            │   └── exceptions
                            │       ├── WalletTransactionDetailCreationException.java
                            │       ├── WalletTransactionDetailDeletionException.java
                            │       ├── WalletTransactionDetailNotFoundException.java
                            │       ├── WalletTransactionDetailRetrievalException.java
                            │       └── WalletTransactionDetailUpdateException.java
                            ├── domain
                            │   ├── entities
                            │   │   └── WalletTransactionDetail.java
                            │   └── enums
                            │       └── WalletTransactionSourceType.java
                            └── infrastructure
                                └── serialization
                                    └── WalletTransactionDetailMapper.java
```
