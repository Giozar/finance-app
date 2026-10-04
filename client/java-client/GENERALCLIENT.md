# The client project has the following structure:
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
│   │   │   │   │   │   │   ├── accountReconciliations
│   │   │   │   │   │   │   │   ├── infrastructure
│   │   │   │   │   │   │   │   │   ├── services
│   │   │   │   │   │   │   │   │   │   ├── AccountReconciliationService.java
│   │   │   │   │   │   │   │   ├── presentation
│   │   │   │   │   │   │   │   │   ├── views
│   │   │   │   │   │   │   │   │   │   ├── AccountReconciliationsView.java
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
│   │   │   │   │   │   │   │   │   │   ├── FormSearchComboBox.java
│   │   │   │   │   │   │   │   │   │   ├── FormMultiSelectField.java
│   │   │   │   │   │   │   │   │   │   ├── FormDateTimeField.java
│   │   │   │   │   │   │   │   │   │   ├── FormHelpText.java
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
│   │   │   │   │   │   │   │   │   ├── QuickCreateDialog.java
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