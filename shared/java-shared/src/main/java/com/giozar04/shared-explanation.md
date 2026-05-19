## Creating and Implementing a New Feature in `java-shared`

The `shared/java-shared` project acts as a shared module between the backend and frontend through RMI. Its main purpose is to centralize common resources that both sides of the application need, avoiding code duplication and ensuring that all layers work with the same data structures.

This module does not contain backend business logic or frontend UI implementation. Instead, it stores shared resources such as entities, enums, exceptions, and utility classes. This allows both the backend and frontend to work with the same models and maintain consistent communication across the application. The project structure follows a layered architecture approach and serves as a common contract between both systems. 

To create a new feature, navigate to the following path:

```text
shared/java-shared/src/main/java/com/giozar04/featureName
```

The recommended general structure for a feature is:

```text
featureName
├── application
│   └── utils
│       └── FeatureNameUtils.java
├── domain
│   ├── entities
│   │   └── FeatureName.java
│   ├── exceptions
│   │   └── FeatureNameExceptions.java
│   └── enums
│       └── FeatureNameTypes.java
```

Not every feature requires all folders. This represents the complete possible structure, but it can be simplified depending on the feature requirements. For example, some entities may not require enums.

## Domain Layer

The `domain` layer contains the core shared structures that define the feature model. These components are used by both backend and frontend systems.

### Entities

The `entities` folder contains the main class representing the feature model. This class defines the structure of the shared data and should include attributes, constructors, getters, setters, and any basic behavior directly related to the entity.

For example, the `Account` entity contains fields such as `id`, `userId`, `name`, `type`, balances, banking details, investment details, timestamps, and additional feature-specific properties. 

```text
domain
└── entities
    └── FeatureName.java
```

### Enums

The `enums` folder contains enumerations used by the feature. Enums allow the system to define controlled values for specific properties and ensure consistency across different modules.

For example, `AccountTypes` defines values such as `CASH`, `DEBIT`, `CREDIT`, `WALLET`, `SAVINGS`, and `INVESTMENT`. 

```text
domain
└── enums
    └── FeatureNameTypes.java
```

### Exceptions

The `exceptions` folder centralizes feature-specific exceptions. These exceptions represent possible error scenarios related to entity operations such as creation, retrieval, updates, deletion, or parsing.

For example, `AccountExceptions` contains exceptions such as `AccountCreationException`, `AccountRetrievalException`, `AccountUpdateException`, and `AccountNotFoundException`. 

```text
domain
└── exceptions
    └── FeatureNameExceptions.java
```

## Application Layer

The `application` layer contains supporting resources used to work with shared entities.

### Utils

The `utils` folder contains utility classes responsible for converting entities into generic structures and reconstructing them later. Since `java-shared` acts as a communication layer between backend and frontend through RMI, these utilities simplify the transfer and interpretation of data across both systems.

Typically, utility classes provide methods to transform an entity into a `Map<String, Object>` and convert a map back into an entity object.

For example, `AccountUtils` provides methods such as `accountToMap()` and `mapToAccount()`, allowing an `Account` object to be serialized into a transferable structure and rebuilt afterward.  

```text
application
└── utils
    └── FeatureNameUtils.java
```

## Simple Feature Example

A simple feature such as `users` may not require enums:

```text
users
├── application
│   └── utils
│       └── UserUtils.java
├── domain
│   ├── exceptions
│   │   └── UserExceptions.java
│   └── entities
│       └── User.java
```

## Complete Feature Example

A more complex feature such as `transactions`, `accounts`, `categories`, `cards`, or `externalEntities` may require entities, enums, exceptions, and utility classes:

```text
accounts
├── application
│   └── utils
│       └── AccountUtils.java
├── domain
│   ├── enums
│   │   └── AccountTypes.java
│   ├── exceptions
│   │   └── AccountExceptions.java
│   └── entities
│       └── Account.java
```

## General Implementation Process for a New Feature

To implement a new feature in `java-shared`, create the feature folder under `com.giozar04`, then create the required `domain` and `application` layers with their corresponding subfolders.

Inside `domain/entities`, create the main entity class. Inside `domain/enums`, create enums only if controlled values are needed. Inside `domain/exceptions`, define feature-specific exceptions. Finally, inside `application/utils`, implement conversion methods between entities and generic structures such as maps.

Once the feature has been created in `java-shared`, both backend and frontend projects can import and use the same shared classes. This ensures that the backend can process business logic and persistence while the frontend can send, receive, and interpret information using exactly the same entities, enums, exceptions, and utility classes defined in the shared module.
