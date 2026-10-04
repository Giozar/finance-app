# Crear una feature en el cliente

[Arquitectura](../../../../../../../ARCHITECTURE.md) · [Estado](../../../../../../../MIGRATION.md)

Cada feature del cliente separa presentación, aplicación e infraestructura. Las vistas
dependen de un puerto de entrada; el caso de uso utiliza un puerto de salida y el
adaptador socket implementa este último. Los modelos y mappers del protocolo vienen
de `java-shared`.

```text
<feature>/
├── application/
│   ├── ports/input/<Feature>Operations.java
│   ├── ports/output/<Feature>Gateway.java
│   └── usecases/<Feature>UseCase.java
├── infrastructure/transport/socket/<Feature>Service.java
└── presentation/
    ├── components/
    └── views/
```

`ApplicationInitializer` crea el adaptador, construye el caso de uso y lo registra
en `ClientUseCases`. Las vistas piden el puerto de entrada al registro. Los
adaptadores socket conservan los códigos de mensajes y la serialización actual.

## Test Layer

The `test` folder contains the functional tests of the feature. These tests are responsible for validating the expected behavior of the module and ensuring that the main operations work correctly.

Usually, two types of tests may exist:

* `FeatureFunctionalTest`: validates the functional behavior of the feature.
* `FeatureGuiFunctionalTest`: validates the graphical interface and complete user interaction flow.

These tests help verify that the feature behaves correctly from the user perspective.

---

## Aplicación e infraestructura

`<Feature>Operations` define lo que necesita la presentación. El caso de uso
implementa ese contrato y depende de `<Feature>Gateway`. El adaptador socket
implementa el gateway: crea `Message`, envía la petición, valida la respuesta y
convierte mapas con el mapper de shared. `ServerConnectionService` y
`ServerResponseValidator` viven en
`serverConnection/infrastructure/transport/socket`. La aplicación no importa
sockets, JSON, Swing ni clases concretas de transporte.

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

#### Form Conventions

* **Formal tone ("usted")**: every visible text (labels, placeholders, validation errors, dialogs) addresses the user as "usted", for example `"Seleccione un usuario..."`, `"Debe seleccionar un usuario propietario."` and `"Corrija los siguientes errores:"`.
* **Enum-based options**: combo boxes and filters that list enum values must be built from `Enum.values()` (for example `OperationTypes.values()`), never from hard-coded arrays, so they stay in sync with `shared`. Display the enum label (`getLabel()`) and compare against the enum itself, not against fixed strings.
* **Owner user selector**: entities that have a `userId` (accounts, tags, categories, external entities) include a `FormComboBox<User>` labeled `"Usuario propietario:"` as the first field, with placeholder `"Seleccione un usuario..."`. Users are loaded through `ClientUseCases.get(UserOperations.class).getAllUsers()`, validation adds `"Debe seleccionar un usuario propietario."` when no valid selection exists, the selected user's id is assigned to `userId`, `loadX(...)` selects the matching user, and `clearForm()` clears the selection. References: `AccountFormPanel`, `TagFormPanel`.
* **Opening balances are read-only when editing**: in `AccountFormPanel` the "Balance actual" field (and "Deuda actual" in `CreditDetailsSubPanel`) is captured only when **creating** an account (it becomes the opening state through the database trigger). When **editing**, both are read-only and show the help text `"Se modifica con transacciones o desde Conciliación."` (`FormHelpText`), so the account never goes out of balance. When creating, an empty value means `0` (if captured it must be a non-negative number).

#### Quick-create pattern

Catalog forms that other screens need to create "on the fly" (`CategoryFormPanel`, `TagFormPanel`, `ExternalEntityFormPanel`) are reused as-is inside a modal dialog instead of being duplicated. They expose:

```java
void setOnSaved(Consumer<T> onSaved)     // called after a successful create/update with the entity RETURNED by the service
void presetUser(User user, boolean lock) // preselects the owner user; lock = disable the combo (kept after clearForm)
void presetType(CategoryTypes type)      // CategoryFormPanel (ExternalEntityTypes in ExternalEntityFormPanel); still editable
```

Without a callback or presets the forms behave exactly as in their own module. Usage with `QuickCreateDialog`:

```java
CategoryFormPanel form = new CategoryFormPanel();
form.presetUser(user, true);
form.presetType(CategoryTypes.EXPENSE);
QuickCreateDialog.show(this, "Nueva categoría", form, form::setOnSaved)
        .ifPresent(category -> { reloadCategories(); categoryCombo.setSelectedItem(category); });
```

The dialog is modal (owner = the window of `parent`), closes as soon as the form calls `onSaved` and returns `Optional.empty()` if the user closes the window. `QuickCreateDialog.open(parent, title, form, form::setOnSaved, onCreated)` is the callback variant.

#### Dynamic form pattern (context + sections + policy)

When a single form must show only what makes sense for the current choices (reference: `transactions`), split it into:

```text
presentation/form/                      (no layout code)
├── <F>FormContext          observable state (Observer); setters notify only on change
├── <F>FormDataProvider     loads and caches catalogs per user through the services (SRP)
├── <Rule>Policy            pure class with the business table (e.g. PaymentMethodPolicy)
└── <F>FormSection          interface: onContextChanged(ctx), validate(errors), applyTo(x), loadFrom(x), clear()
presentation/components/
├── sections/               one JPanel per section (extends AbstractTransactionSection)
└── <F>FormPanel            orchestrator: composes sections, validates, builds the aggregate, saves
```

Rules of the pattern:

* **Sections write to the context, never to each other.** Each section publishes what the user chooses
  (`ctx.setOperation(...)`, `ctx.setSourceAccount(...)`) and reacts in `onContextChanged`: show/hide rows,
  reload its items and **clear what no longer applies** (e.g. switching from CARD to WALLET clears the card).
* **Notifications are coalesced**: if a listener changes the context while it is notifying, a new round runs
  afterwards, so every section always sees the final state. Sections must be idempotent (track the last user,
  operation or account they loaded for and only reload when it changes).
* **The data provider is the first listener** (`ctx -> provider.loadForUser(ctx.getUserId())`), so catalogs are
  loaded before sections react. Errors go to `provider.setOnError(...)`.
* **Business tables live in a pure policy class** (`PaymentMethodPolicy.allowedMethods(operation, sourceType,
  destinationType)`); the section only renders the result (auto-select and lock when only one option remains).
* **The orchestrator** calls `validate` on every section (one combined error dialog), builds a new aggregate with
  `applyTo` in section order, and for editing calls `loadFrom` in section order after selecting the user, because
  each section depends on the context set by the previous ones.
* `AbstractTransactionSection` provides the titled `BoxLayout` panel, `addRow`/`setRowVisible` (hides the row and
  its spacer) and `selectInCombo(combo, predicate)` (entities do not implement `equals`).

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
* `FormSearchComboBox` / `FormMultiSelectField` / `FormDateTimeField` / `FormHelpText` (see below)
* `QuickCreateDialog`
* `GenericTablePanel`
* `DialogUtil`
* `FormValidatorUtils`

Search, multi-select and date-time fields (`shared/components/forms`):

* `FormSearchComboBox<T>`: label + editable combo that filters while typing (by `toString()` or `setDisplayFunction(Function<T,String>)`). Same API as `FormComboBox` (`setPlaceholder`, `setItems`, `getSelectedItem`, `setSelectedItem`, `isSelectionValid`, `clearSelection`, `addActionListener`, `setEnabled`) plus `setIdentityFunction(Function<T,?>)` (e.g. `User::getId`, because entities do not implement `equals`), `getItems`, `getItemCount`, `getItemAt`. Listeners fire only when the confirmed selection changes, never while filtering. The placeholder is drawn as a hint, it is not a list item.
* `FormMultiSelectField<T>`: label + search box + removable "chips" (✕). API: `setItems`, `getSelectedItems`, `setSelectedItems`, `addSelected(T)` (adds a newly created item already selected), `clear`, `setOnCreateNew(Runnable)` (shows "+ Nueva"), `setPlaceholder`, `setDisplayFunction`, `setIdentityFunction`, `addChangeListener`, `setEnabled`.
* `FormDateTimeField`: label + `DatePickerComponent` + `HH:mm` spinner. `getDateTime()` returns a `ZonedDateTime` in the system zone, `setDateTime(ZonedDateTime)` (converted to the system zone keeping the instant), `clearToNow()`, `getZoneId()`.
* `FormHelpText`: small grey help text aligned with the field column (`new FormHelpText(text, width)`, `setText`).

Before creating a new component, this folder should always be reviewed first to avoid code duplication and encourage reuse. 

---

## Conexión y arranque

`ApplicationInitializer` abre la conexión mediante
`serverConnection/infrastructure/transport/socket/ServerConnectionService`, registra
cada caso de uso en `ClientUseCases` y arranca Swing. El registro pertenece a
`bootstrap`; las vistas acceden solo a las interfaces `*Operations`.

Al crear una feature, añada sus puertos, caso de uso y adaptador socket, registre
el caso de uso en bootstrap y conecte las vistas con el puerto de entrada. Actualice
el agente, `MIGRATION.md` y los índices. Compile los tres módulos y compare los
contratos con `python3 scripts/verify_shared.py`. Los formularios y componentes
reutilizables se describen en las secciones anteriores.
