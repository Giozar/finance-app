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
