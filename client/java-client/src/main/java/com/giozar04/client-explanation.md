# Implementing a New Feature in the Client Project

To create a new feature within the `client` project, the first step is to navigate to the main package where all application modules are organized. Features are created under the following path:

```text
client/java-client/src/main/java/com/giozar04/
```

Inside this location, a new folder should be created using the feature name. For example:

```text
com/giozar04/featureName
```

The client project follows a layered architecture where each feature separates responsibilities into independent modules. The overall system is composed of three main projects: `backend`, `client`, and `RMI/shared`. The backend handles business logic and persistence, the client manages the user interface and interactions, and the `RMI/shared` project contains resources shared between both applications, such as entities, enums, exceptions, and common utilities.

From the client perspective, the responsibility of a feature is to provide the visual layer and communicate with the backend through the socket infrastructure already implemented.

A typical client feature follows a structure similar to the following:

```text
featureName
├── test
│   ├── FeatureNameFunctionalTest.java
│   └── FeatureNameGuiFunctionalTest.java
├── infrastructure
│   └── services
│       └── FeatureNameService.java
└── presentation
    ├── components
    │   ├── FeatureNameFormPanel.java
    │   └── subpanels
    │       └── FeatureNameDetailsSubPanel.java
    └── views
        ├── FeatureNamesView.java
        └── CreateFeatureNameView.java
```

## Test Layer

The `test` folder contains the functional tests of the feature. These tests are responsible for validating the expected behavior of the module and ensuring that the main operations work correctly.

Usually, two types of tests may exist:

* `FeatureFunctionalTest`: validates the functional behavior of the feature.
* `FeatureGuiFunctionalTest`: validates the graphical interface and complete user interaction flow.

These tests help verify that the feature behaves correctly from the user perspective.

---

## Infrastructure Layer

The `infrastructure` layer contains the services responsible for communication with the backend.

Inside `infrastructure/services`, a class such as `FeatureNameService` is created. Its responsibility is to:

* Build request messages.
* Define operation types.
* Send requests to the server.
* Wait for responses.
* Validate received data.
* Convert response data into application entities.

This pattern can be observed in `AccountService`, where messages such as `CREATE_ACCOUNT`, `UPDATE_ACCOUNT`, `DELETE_ACCOUNT`, and `GET_ALL_ACCOUNTS` are created and sent through `ServerConnectionService`. After receiving a response, the service validates the server output and transforms the returned data into application entities. 

The service layer should never contain UI logic; its responsibility is limited to communication and data handling.

---

## Presentation Layer

The `presentation` layer contains all visual elements and user interaction components associated with the feature.

This layer is divided into components and views.

### Components

The `components` folder contains reusable visual elements specific to the feature.

A common example is `FeatureFormPanel`, which acts as the main form component responsible for:

* Creating form fields.
* Loading data from services.
* Performing validations.
* Dynamically showing or hiding components.
* Building entities before sending them to the backend.
* Calling service operations.

This behavior can be seen in `AccountFormPanel`, where user information is loaded, account types are selected, validations are executed, and different sections become visible depending on the selected account type. 

When a form becomes large or contains multiple specialized sections, it is recommended to divide it into subpanels.

For example:

* `BankDetailsSubPanel` handles bank-related information such as bank client, account number, and CLABE. 
* `CreditDetailsSubPanel` manages credit-specific fields such as credit limit, cutoff day, and payment day. 
* `SavingsDetailsSubPanel` manages annual yield and savings configuration. 
* `InvestmentDetailsSubPanel` manages investment-specific configurations such as instrument types, maturity dates, annual yield, and reinvestment settings. 

All subpanels should expose a consistent contract through methods such as:

```java
validate()
applyTo()
loadFrom()
clear()
```

This approach improves maintainability and follows SOLID principles.

---

### Views

The `views` folder contains the main screens displayed to the user.

Normally, a feature contains:

```text
FeatureNamesView
CreateFeatureNameView
```

`FeatureNamesView` acts as the main module screen and is responsible for:

* Displaying records in tables
* Loading data
* Managing searches
* Handling edit operations
* Handling delete operations
* Opening creation views

For example, `AccountsView` retrieves account data using `AccountService`, renders the data inside a generic table, performs searches, and handles edit and delete actions. 

`CreateFeatureNameView` usually acts as a lightweight container whose only responsibility is rendering the form component.

For example, `CreateAccountView` simply creates an `AccountFormPanel` and adds it to the view. 

---

## Shared Resources

The client project also contains a `shared` folder that stores reusable resources used by multiple features.

Examples include:

```text
shared
├── utils
├── components
├── layouts
```

Reusable components include:

* `FormField`
* `FormComboBox`
* `FormTextArea`
* `FormDateField`
* `PercentageField`
* `GenericTablePanel`
* `DialogUtil`
* `FormValidatorUtils`

Before creating a new component, this folder should always be reviewed first to avoid code duplication and encourage reuse. 

---

## Server Connection Layer

The project also includes a dedicated `serverConnection` module responsible for managing communication with the backend.

Since the application uses sockets for communication, this module manages:

* Server configuration
* Connection initialization
* Sending messages
* Receiving responses
* Response validation
* Connection abstractions

This layer provides the infrastructure required by all services.

---

## Application Initialization

The application startup process is managed by `ApplicationInitializer`.

During application startup:

1. A connection with the backend server is established.
2. Service instances are initialized.
3. Dependencies become available globally.
4. The graphical interface is launched.

This process ensures that all services required by the application are connected before the user interface becomes available. 

---

## General Feature Implementation Flow

The complete process for creating a new feature in the client project can be summarized as follows:

```text
1. Create the feature folder inside com/giozar04.
2. Create the infrastructure/services layer.
3. Implement backend communication methods using Message objects.
4. Create presentation/components for forms and custom UI elements.
5. Create subpanels if specialized sections are required.
6. Create the main views inside presentation/views.
7. Reuse existing resources from shared whenever possible.
8. Register and initialize the service in ApplicationInitializer.
9. Verify that backend and RMI/shared resources exist and are compatible.
```

In general, the application flow works as follows: `ApplicationInitializer` starts the application, `ServerConnectionService` establishes communication with the backend, feature services perform the required operations, and presentation components render and manage information for the user interface.
