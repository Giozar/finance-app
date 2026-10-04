> Migración por features: consulte [ARCHITECTURE.md](../../../../../../../ARCHITECTURE.md), [MIGRATION.md](../../../../../../../MIGRATION.md) y [AGENTS.md](../../../../../../../AGENTS.md). Las features pendientes conservan la estructura documentada aquí.

# Implementing a New Feature in the Backend

The project is organized using a layered architecture and divided into different modules. At a high level, the system consists of a backend, a frontend, and a shared RMI module containing common elements used by both sides of the application. Because of this, some classes or definitions are not located directly inside the backend but rather inside the shared project.

Within the backend, each main functionality is organized as an independent feature. This approach keeps the code modular, organized, and easy to extend. To create a new feature, navigate to the following path:

```txt
backend/java-server/src/main/java/com/giozar04/
```

Inside this location, create a new folder with the feature name. For example:

```txt
backend/java-server/src/main/java/com/giozar04/featureName
```

The base structure of a feature follows this pattern:

```txt
featureName
├── test
│   └── FeatureTestApp.java
├── application
│   └── services
│       └── FeatureService.java
├── infrastructure
│   ├── repositories
│   │   └── FeatureRepositoryMySQL.java
│   ├── controllers
│   │   └── FeatureControllers.java
│   └── handlers
│       └── FeatureHandlers.java
├── domain
│   ├── models
│   │   └── FeatureRepositoryAbstract.java
│   └── interfaces
│       └── FeatureRepositoryInterface.java
└── sql
    └── feature.sql
```

This structure is repeated for most entities in the system, such as:

```txt
users
accounts
transactions
categories
tags
cards
bankClients
externalEntities
walletCardLinks
walletTransactionDetails
cardTransactionDetails
accountCashbackSettings
accountReconciliations (solo lectura sobre la vista v_account_reconciliation + procedimiento sp_reconcile_account)
```

## General Flow Overview

The general request flow inside a feature is the following:

```txt
Client
↓
JSON Message
↓
ServerService
↓
FeatureHandlers
↓
FeatureControllers
↓
FeatureService
↓
FeatureRepositoryInterface
↓
FeatureRepositoryAbstract
↓
FeatureRepositoryMySQL
↓
Database
```

`ServerService` receives messages from the client and searches for a registered handler for the incoming message type. Handlers determine which controller should execute a given operation. The controller processes the request, extracts and transforms the data if needed, and then calls the service layer. The service delegates the request to the repository, and finally the concrete repository implementation executes SQL operations against the database. This message processing flow can be observed in `ServerService`, where incoming messages are routed to the corresponding handler. 

## `domain/interfaces`

This folder contains the main repository contract:

```txt
FeatureRepositoryInterface.java
```

The interface declares all operations available for the feature, for example:

```java
createFeature()
getFeatureById()
updateFeatureById()
deleteFeatureById()
getAllFeatures()
```

Its purpose is to define what operations are available without specifying how they are implemented. In `AccountRepositoryInterface`, the basic CRUD methods for the entity are declared. 

## `domain/models`

This folder contains the abstract repository implementation:

```txt
FeatureRepositoryAbstract.java
```

This class implements the repository interface and serves as a common base for concrete implementations. Its main responsibility is to centralize validations, shared logic, and reusable dependencies.

Examples include:

* Entity validation
* ID validation
* Shared database connection access
* Logger access
* Reusable helper methods
* Common business rules

In `AccountRepositoryAbstract`, database connection management, logging functionality, and account validation logic are centralized. 

## `application/services`

This folder contains the service class:

```txt
FeatureService.java
```

The service implements the same repository interface and receives an instance of `FeatureRepositoryInterface` through dependency injection.

Its responsibility is to act as an intermediate layer between controllers and repositories. In this project, the service mainly delegates execution to the repository instance. In `AccountService`, each method forwards the operation directly to the internal repository. 

## `infrastructure/repositories`

This folder contains the concrete repository implementation:

```txt
FeatureRepositoryMySQL.java
```

This class extends `FeatureRepositoryAbstract` and contains the actual persistence logic.

Typical responsibilities include:

* SQL queries
* PreparedStatements
* ResultSet processing
* Transactions
* Commit and rollback operations
* Mapping database records into entities

For example, `AccountRepositoryMySQL` implements SQL operations for creating, updating, retrieving, and deleting accounts. 

Database rules and persistence notes:

* Business rules enforced by the database (triggers with `SIGNAL SQLSTATE '45000'` and `CHECK` constraints) reach the repository as a `SQLException`. Propagate its message in the shared exception (e.g. `"Error al crear transacción: " + e.getMessage()`) so it reaches the client through `ServerService` (`"Error al procesar solicitud: ..."`).
* Enums are persisted with `getValue()` (UPPERCASE values). They must match the values allowed by the corresponding `CHECK` constraint in `database/schemas.sql`.

## `infrastructure/controllers`

This folder contains feature controllers:

```txt
FeatureControllers.java
```

These controllers are not traditional HTTP controllers. Instead, they work as adapters between socket messages and business logic.

Their responsibilities include:

* Receiving a `Message`
* Extracting data
* Validating input
* Transforming data into entities
* Calling the service layer
* Returning a response message

For example, `AccountControllers` receives account data, converts it into an `Account` object, calls `accountService.createAccount()`, and builds a response message. 

## `infrastructure/handlers`

This folder contains feature handlers:

```txt
FeatureHandlers.java
```

Handlers are responsible for registering the message types supported by the server and connecting them to their corresponding controllers.

For example, `AccountHandlers` registers operations such as:

```txt
CREATE_ACCOUNT
GET_ACCOUNT
UPDATE_ACCOUNT
DELETE_ACCOUNT
GET_ALL_ACCOUNTS
```

Each message type is associated with its respective controller. 

## `sql`

This folder contains SQL scripts associated with the feature:

```txt
feature.sql
```

These scripts may contain:

* Table creation
* Foreign keys
* Relationships
* Database initialization scripts

Although these files are not directly involved in runtime execution, they serve as structural documentation and database setup resources.

Schema changes that affect existing data are not applied by editing these files: they belong to the database module as a migration in `database/migrations/` (and in `database/schemas.sql`).

## `test`

This folder contains a console-based test application:

```txt
FeatureTestApp.java
```

Its purpose is to test the feature directly without relying on the frontend.

Typically, it allows:

* Creating records
* Retrieving records
* Updating records
* Deleting records
* Listing all records

For example, `AccountTestApp` initializes the database connection, creates the repository and service instances, and provides a console menu for CRUD operations. 

## Registering the New Feature

Creating folders and classes is not enough for the feature to become functional. The feature must also be registered during application startup.

The main location for this process is:

```txt
bootstrap/ApplicationInitializer.java
```

Inside this class, create the repository, service, and handler instances:

```java
FeatureRepositoryInterface featureRepository =
        new FeatureRepositoryMySQL(dbConnection);

FeatureService featureService =
        new FeatureService(featureRepository);
```

Then register the handler:

```java
List<ServerRegisterHandlers> featureServices = List.of(
    new FeatureHandlers(featureService)
);
```

`ApplicationInitializer` follows this same pattern for every feature: repository initialization, service initialization, and handler registration. 

Finally, `ServerInitializer` receives all handlers and registers them inside `ServerService`. 

## Multi-table Operations: `TransactionalExecutor`

Most features write a single table through the shared connection (`databaseConnection.getConnection()` + `commitTransaction()`). When one operation must write **several tables atomically**, use the unit-of-work pattern instead:

```txt
databases/application/services/TransactionalExecutor.java   <T> T inTransaction(SqlWork<T> work) throws SQLException
databases/domain/interfaces/SqlWork.java                    T execute(Connection c) throws SQLException
DatabaseConnectionInterface.createConnection()              new dedicated connection, autocommit = false
```

`inTransaction` opens a dedicated connection, runs the work, commits, rolls back on **any** exception (rethrowing it unchanged) and closes the connection. Because it never touches the shared connection, it is thread-safe.

Rules for the participating repositories:

* Expose methods that **receive the `Connection`** (e.g. `insert(Connection, detail)`, `deleteByTransactionId(Connection, id)`, `findByTransactionId(Connection, id)`). They never commit, roll back or close it.
* Declare them in a separate interface (e.g. `CardTransactionDetailTransactionalRepositoryInterface`) so the service-facing CRUD interface does not leak `Connection`.
* The classic CRUD methods reuse those same methods (no duplicated SQL).
* The caller wraps the `SQLException` into its feature exception **including `e.getMessage()`**, so trigger/CHECK messages reach the client.

Reference: `transactions/infrastructure/repositories/TransactionRepositoryMySQL` (transaction + card/wallet detail + `transaction_tags`). Insertion order matters because the detail triggers validate against the parent row:

```txt
create: INSERT transactions → INSERT detail → replaceTags
update: DELETE details → UPDATE transactions → INSERT detail → replaceTags
delete: DELETE transactions (FK cascades + trigger 4.1 do the rest)
```

## Business Rules by Strategy (`transactions`)

When an entity has many conditional rules, keep them out of the repository and use small strategies:

```txt
transactions/application/validation/
├── TransactionRule            void validate(Transaction tx, ValidationContext ctx, List<String> errors)
├── ValidationContext          per-request cached lookups through EXISTING repository interfaces (DIP)
├── ValidationContextFactory   creates a fresh context (empty cache) per request
├── EnumDispatchRule<E>        EnumMap<E, TransactionRule>; fails at startup if an enum value has no rule
├── TransactionRules           default rule set (common + by operation type + by payment method)
├── TransactionValidator       composite: runs every rule, joins errors with "; ",
│                              throws TransactionValidationException (shared)
└── rules/                     CommonFieldsRule, IncomeRule, ExpenseRule, ReallocationRule,
                               CardPaymentRule, WalletPaymentRule, InternalPaymentRule, NoDetailPaymentRule
transactions/application/normalizers/TransactionNormalizer   derived data the user does not type
```

`TransactionService` runs **normalize → validate → repository** for create/update, sharing one `ValidationContext`. Rules only add messages (in Spanish) and never throw. To add a rule, implement `TransactionRule` and register it in `TransactionRules` (or in the matching `EnumMap`). The database keeps its own checks as the last line of defence; backend messages should be equivalent to the `SIGNAL` texts.

## Summary

To create a fully functional feature in the backend, the following steps should be completed:

```txt
1. Create the feature folder under com/giozar04
2. Create the internal layered structure
3. Define the repository contract in domain/interfaces
4. Create the abstract repository in domain/models
5. Create the service in application/services
6. Create the concrete repository implementation
7. Create controllers
8. Create handlers
9. Create SQL scripts
10. Create a console test application
11. Register the repository, service, and handler in ApplicationInitializer
```

Overall, this architecture allows each feature to remain independent, maintainable, and easily integrated into the main server infrastructure.
