# Arquitectura de client

Mapa de la estructura actual del cliente.

[Arquitectura común](../../ARCHITECTURE.md) · [Guía](CLIENT_GUIDE.md) · [Agente](../../.claude/agents/finance-app-expert-client.md)

## Árbol del módulo

```text
└── java-client
    ├── pom.xml
    ├── .gitignore
    ├── CLIENT_ARCHITECTURE.md
    ├── CLIENT_GUIDE.md
    └── src
        ├── test
        │   └── java
        │       └── TestTable.java
        └── main
            ├── resources
            │   ├── config.example.properties
            │   └── config.properties          (local, ignorado por Git)
            └── java
                └── com
                    └── giozar04
                        ├── accountCashbackSettings
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── AccountCashbackSettingOperations.java
                        │   │   │   └── output
                        │   │   │       └── AccountCashbackSettingGateway.java
                        │   │   └── usecases
                        │   │       └── AccountCashbackSettingUseCase.java
                        │   └── infrastructure
                        │       └── transport
                        │           └── socket
                        │               └── AccountCashbackSettingService.java
                        ├── accountReconciliations
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── AccountReconciliationOperations.java
                        │   │   │   └── output
                        │   │   │       └── AccountReconciliationGateway.java
                        │   │   └── usecases
                        │   │       └── AccountReconciliationUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── AccountReconciliationService.java
                        │   └── presentation
                        │       └── views
                        │           └── AccountReconciliationsView.java
                        ├── accounts
                        │   ├── test
                        │   │   ├── AccountFunctionalTest.java
                        │   │   └── AccountGuiFunctionalTest.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── AccountOperations.java
                        │   │   │   └── output
                        │   │   │       └── AccountGateway.java
                        │   │   └── usecases
                        │   │       └── AccountUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── AccountService.java
                        │   └── presentation
                        │       ├── components
                        │       │   ├── AccountFormPanel.java
                        │       │   ├── FinancialSummaryPanel.java
                        │       │   └── subpanels
                        │       │       ├── BankDetailsSubPanel.java
                        │       │       ├── CashbackSettingsPanel.java
                        │       │       ├── CreditDetailsSubPanel.java
                        │       │       ├── InvestmentDetailsSubPanel.java
                        │       │       ├── SavingsDetailsSubPanel.java
                        │       │       └── WalletCardLinksPanel.java
                        │       └── views
                        │           ├── AccountDetailView.java
                        │           ├── AccountsView.java
                        │           ├── CreateAccountView.java
                        │           └── detail
                        │               ├── BaseAccountDetailView.java
                        │               ├── BenefitAccountDetailView.java
                        │               ├── CashAccountDetailView.java
                        │               ├── CreditAccountDetailView.java
                        │               ├── DebitAccountDetailView.java
                        │               ├── InvestmentAccountDetailView.java
                        │               ├── SavingsAccountDetailView.java
                        │               └── WalletAccountDetailView.java
                        ├── bankClients
                        │   ├── test
                        │   │   ├── BankClientFunctionalTest.java
                        │   │   └── BankClientGuiFunctionalTest.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── BankClientOperations.java
                        │   │   │   └── output
                        │   │   │       └── BankClientGateway.java
                        │   │   └── usecases
                        │   │       └── BankClientUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── BankClientService.java
                        │   └── presentation
                        │       ├── components
                        │       │   ├── BankClientFormPanel.java
                        │       │   └── BankNameCellRenderer.java
                        │       └── views
                        │           ├── BankClientsView.java
                        │           └── CreateBankClientView.java
                        ├── bootstrap
                        │   ├── ApplicationInitializer.java
                        │   └── ClientUseCases.java
                        ├── cards
                        │   ├── test
                        │   │   └── CardCreationTest.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── CardOperations.java
                        │   │   │   └── output
                        │   │   │       └── CardGateway.java
                        │   │   └── usecases
                        │   │       └── CardUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── CardService.java
                        │   └── presentation
                        │       ├── components
                        │       │   └── CardFormPanel.java
                        │       └── views
                        │           ├── CardsView.java
                        │           └── CreateCardView.java
                        ├── cardTransactionDetails
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── CardTransactionDetailOperations.java
                        │   │   │   └── output
                        │   │   │       └── CardTransactionDetailGateway.java
                        │   │   └── usecases
                        │   │       └── CardTransactionDetailUseCase.java
                        │   └── infrastructure
                        │       └── transport
                        │           └── socket
                        │               └── CardTransactionDetailService.java
                        ├── categories
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── CategoryOperations.java
                        │   │   │   └── output
                        │   │   │       └── CategoryGateway.java
                        │   │   └── usecases
                        │   │       └── CategoryUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── CategoryService.java
                        │   └── presentation
                        │       ├── components
                        │       │   └── CategoryFormPanel.java
                        │       └── views
                        │           ├── CategoriesView.java
                        │           └── CreateCategoryView.java
                        ├── configs
                        │   ├── AppConfig.java
                        │   └── ServerConnectionConfig.java
                        ├── dashboard
                        │   └── presentation
                        │       └── views
                        │           └── MainDashboardView.java
                        ├── externalEntities
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── ExternalEntityOperations.java
                        │   │   │   └── output
                        │   │   │       └── ExternalEntityGateway.java
                        │   │   └── usecases
                        │   │       └── ExternalEntityUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── ExternalEntityService.java
                        │   └── presentation
                        │       ├── components
                        │       │   └── ExternalEntityFormPanel.java
                        │       └── views
                        │           ├── CreateExternalEntityView.java
                        │           └── ExternalEntitiesView.java
                        ├── Main.java
                        ├── serverConnection
                        │   ├── application
                        │   │   └── exceptions
                        │   │       └── ClientOperationException.java
                        │   └── infrastructure
                        │       └── transport
                        │           └── socket
                        │               ├── ServerConnectionAbstract.java
                        │               ├── ServerConnectionConfig.java
                        │               ├── ServerConnectionInterface.java
                        │               ├── ServerConnectionService.java
                        │               └── ServerResponseValidator.java
                        ├── shared
                        │   ├── components
                        │   │   ├── CreditUsagePanel.java
                        │   │   ├── DatePickerComponent.java
                        │   │   ├── forms
                        │   │   │   ├── ColorPickerField.java
                        │   │   │   ├── FormComboBox.java
                        │   │   │   ├── FormDateField.java
                        │   │   │   ├── FormDateTimeField.java
                        │   │   │   ├── FormField.java
                        │   │   │   ├── FormHelpText.java
                        │   │   │   ├── FormLabel.java
                        │   │   │   ├── FormMultiSelectField.java
                        │   │   │   ├── FormSearchComboBox.java
                        │   │   │   ├── FormTextArea.java
                        │   │   │   └── PercentageField.java
                        │   │   ├── HeaderPanel.java
                        │   │   ├── MainContentPanel.java
                        │   │   ├── QuickCreateDialog.java
                        │   │   ├── SidebarPanel.java
                        │   │   └── table
                        │   │       ├── ColumnDefinition.java
                        │   │       ├── GenericTableModel.java
                        │   │       ├── GenericTablePanel.java
                        │   │       ├── OptionsCellEditor.java
                        │   │       ├── OptionsCellRenderer.java
                        │   │       └── PopupMenuActionHandler.java
                        │   ├── layouts
                        │   │   └── AppLayout.java
                        │   └── utils
                        │       ├── DialogUtil.java
                        │       └── FormValidatorUtils.java
                        ├── tags
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── TagOperations.java
                        │   │   │   └── output
                        │   │   │       └── TagGateway.java
                        │   │   └── usecases
                        │   │       └── TagUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── TagService.java
                        │   └── presentation
                        │       ├── components
                        │       │   └── TagFormPanel.java
                        │       └── views
                        │           ├── CreateTagView.java
                        │           └── TagsView.java
                        ├── transactions
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── TransactionOperations.java
                        │   │   │   └── output
                        │   │   │       └── TransactionGateway.java
                        │   │   └── usecases
                        │   │       └── TransactionUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── TransactionService.java
                        │   └── presentation
                        │       ├── components
                        │       │   ├── AccountPickerField.java
                        │       │   ├── CreatableSearchField.java
                        │       │   ├── PaymentMethodCellRenderer.java
                        │       │   ├── sections
                        │       │   │   ├── AbstractTransactionSection.java
                        │       │   │   ├── CardDetailsSection.java
                        │       │   │   ├── ClassificationSection.java
                        │       │   │   ├── GeneralInfoSection.java
                        │       │   │   ├── OperationSection.java
                        │       │   │   ├── PartiesSection.java
                        │       │   │   ├── PaymentMethodSection.java
                        │       │   │   └── WalletDetailsSection.java
                        │       │   ├── TransactionDetailsDialog.java
                        │       │   ├── TransactionFormPanel.java
                        │       │   ├── TransactionNameLookup.java
                        │       │   └── TransactionTypeCellRenderer.java
                        │       ├── form
                        │       │   ├── PaymentMethodPolicy.java
                        │       │   ├── TransactionFormContext.java
                        │       │   ├── TransactionFormDataProvider.java
                        │       │   └── TransactionFormSection.java
                        │       └── views
                        │           ├── CreateTransactionView.java
                        │           └── TransactionsView.java
                        ├── users
                        │   ├── test
                        │   │   ├── UserFunctionalTest.java
                        │   │   └── UserGuiFunctionalTest.java
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── UserOperations.java
                        │   │   │   └── output
                        │   │   │       └── UserGateway.java
                        │   │   └── usecases
                        │   │       └── UserUseCase.java
                        │   ├── infrastructure
                        │   │   └── transport
                        │   │       └── socket
                        │   │           └── UserService.java
                        │   └── presentation
                        │       ├── components
                        │       │   └── UserFormPanel.java
                        │       └── views
                        │           ├── CreateUserView.java
                        │           └── UsersView.java
                        ├── walletCardLinks
                        │   ├── application
                        │   │   ├── ports
                        │   │   │   ├── input
                        │   │   │   │   └── WalletCardLinkOperations.java
                        │   │   │   └── output
                        │   │   │       └── WalletCardLinkGateway.java
                        │   │   └── usecases
                        │   │       └── WalletCardLinkUseCase.java
                        │   └── infrastructure
                        │       └── transport
                        │           └── socket
                        │               └── WalletCardLinkService.java
                        └── walletTransactionDetails
                            ├── application
                            │   ├── ports
                            │   │   ├── input
                            │   │   │   └── WalletTransactionDetailOperations.java
                            │   │   └── output
                            │   │       └── WalletTransactionDetailGateway.java
                            │   └── usecases
                            │       └── WalletTransactionDetailUseCase.java
                            └── infrastructure
                                └── transport
                                    └── socket
                                        └── WalletTransactionDetailService.java
```
