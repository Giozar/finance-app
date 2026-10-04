# Guía de client

[Arquitectura común](../../ARCHITECTURE.md) · [Árbol de client](CLIENT_ARCHITECTURE.md) · [Agente](../../.claude/agents/finance-app-expert-client.md)

## Para qué sirve

`java-client` es la aplicación de escritorio Swing. Presenta información, recoge las
acciones del usuario y se comunica con `java-server` por sockets mediante mensajes
JSON. Las entidades, enums, excepciones y mappers del protocolo vienen del JAR
`java-shared`. Las reglas de negocio y los efectos sobre saldos se validan en el
servidor; el cliente valida los formularios y muestra solo las opciones pertinentes.

La configuración (`server.host`, `server.port`) se lee de
`src/main/resources/config.properties` (plantilla: `config.example.properties`; véase
el [README](../../README.md#2-configuración)).

La carpeta `com/giozar04/shared/` contiene componentes visuales reutilizables del
cliente; no es el módulo `shared/java-shared`.

## Cómo está construido

El código vive en `src/main/java/com/giozar04/`:

| Paquete | Responsabilidad |
| --- | --- |
| `Main` | Llama a `bootstrap/ApplicationInitializer.start()` |
| `bootstrap/` | `ApplicationInitializer` (composition root) y `ClientUseCases` (registro de puertos de entrada) |
| `configs/` | `AppConfig` lee `config.properties`; `ServerConnectionConfig` expone host y puerto |
| `serverConnection/` | Conexión por socket, espera de respuestas y `ClientOperationException` |
| `shared/` | Componentes, layouts y utilidades Swing reutilizables |
| `dashboard/` | Vista de inicio |
| `<feature>/` | Una carpeta por feature con las capas que usa |

Una feature de cliente tiene esta estructura:

```text
<feature>/
├── application/
│   ├── ports/input/<Entity>Operations.java       # lo que usa la presentación
│   ├── ports/output/<Entity>Gateway.java         # lo que necesita del servidor
│   └── usecases/<Entity>UseCase.java             # implementa Operations y delega en Gateway
├── infrastructure/transport/socket/<Entity>Service.java   # implementa Gateway
├── presentation/
│   ├── components/                               # formularios y paneles
│   └── views/                                    # pantallas
└── test/                                         # pruebas manuales (si existen)
```

Las features sin pantalla propia (`accountCashbackSettings`, `walletCardLinks`,
`cardTransactionDetails`, `walletTransactionDetails`) solo tienen `application` e
`infrastructure`. La configuración de cashback y los vínculos wallet ↔ tarjeta se
editan desde el formulario de cuentas y los consulta el de transacciones. Los detalles
de tarjeta y wallet viajan dentro del agregado `Transaction`; sus puertos están
registrados, pero ninguna pantalla los usa.

## Arranque y recorrido

```text
Main → ApplicationInitializer.start()
  1. ServerConnectionService.getInstance(host, port).connect()
  2. <Entity>Service.connectService(conexión)            # gateway socket
     ClientUseCases.register(<Entity>Operations.class, new <Entity>UseCase(gateway))
  3. Se muestra AppLayout

Vista/Formulario → ClientUseCases.get(<Entity>Operations.class) → UseCase → Gateway
  → <Entity>Service: Message(type, data) → sendMessage → waitForMessage(type)
  → ServerResponseValidator.validateResponse → Mapper.fromMap → entidad
```

- `ServerConnectionService` lee las respuestas en un hilo y las encola por `type`;
  `waitForMessage(type)` espera la respuesta del mismo tipo que la solicitud.
- `ServerResponseValidator` lanza `ClientOperationException` si no hay respuesta o si
  su estado es `ERROR`; el mensaje es el `content` que envió el servidor.
- Los servicios socket escriben los tipos de mensaje como texto (`"CREATE_TAG"`); deben
  coincidir con los valores del backend.
- Si no hay conexión al arrancar, la aplicación registra el error y no abre la UI.

## Cómo implementar una feature

1. Revise [el árbol](CLIENT_ARCHITECTURE.md), una feature parecida y el contrato en
   shared. Confirme los tipos de mensaje en backend:
   `rg -n "GET_ALL_TAGS" backend/java-server/src`.
2. Defina `*Operations`, `*Gateway` y `*UseCase` para las operaciones que la interfaz
   necesita. Mantenga los casos de uso fuera de Swing y del protocolo.
3. Implemente `<Entity>Service` como gateway, con `connectService`/`getInstance`, los
   códigos y claves del backend, `ServerResponseValidator` y las excepciones de
   operación de shared.
4. En `bootstrap/ApplicationInitializer`, conecte el servicio y registre el caso de
   uso en `ClientUseCases`.
5. Cree o actualice los paneles y vistas bajo `presentation`. Obtenga los puertos con
   `ClientUseCases.get(...)`. Si la feature va en el menú, añada la entrada al arreglo
   `menuItems` de `SidebarPanel` y su `case` en `AppLayout.navigate(...)`.
6. Compile después de instalar shared y pruebe la pantalla con el servidor en ejecución.
7. Actualice `CLIENT_ARCHITECTURE.md` si cambia el árbol, este documento si cambia el
   flujo y el agente client si cambian pantallas, contratos o reglas.

## Convenciones de la interfaz

- Reutilice los componentes de `shared/`: formularios (`components/forms`), tablas
  (`components/table`), `QuickCreateDialog`, `DialogUtil`, `FormValidatorUtils` y
  `AppLayout`. Un componente nuevo va en `shared/components/` si sirve a varias
  features, o en `<feature>/presentation/components/` si es específico.
- Los formularios grandes se dividen en paneles especializados. El panel principal
  coordina validación, lectura y escritura del modelo; las secciones no se llaman
  entre sí.
- Los combos de enums se construyen con `Enum.values()` y muestran `getLabel()`.
- Los textos visibles y los mensajes de validación se redactan en tono formal de usted.
- Las features con `userId` muestran primero el selector del usuario propietario.
  Para alta rápida de categorías, etiquetas o entidades externas se reutilizan sus
  formularios con `QuickCreateDialog` y `setOnSaved`.
- Los saldos iniciales de cuentas son de solo lectura. Los saldos actuales se capturan
  al crear la cuenta; al editarla se modifican mediante transacciones o conciliación.
- Los importes se muestran con `String.format("$%,.2f", valor)`.

## Formulario de transactions

`Transaction` representa un agregado que se envía en un único mensaje. La pantalla
usa `TransactionFormContext` para compartir usuario, operación, cuentas, método y
monto; `TransactionFormDataProvider` carga y cachea catálogos. Cada sección
implementa `TransactionFormSection`: observa el contexto, valida y aplica sus datos al
agregado. `TransactionFormPanel` coordina las secciones y guarda el resultado.

Las secciones son operación, participantes, método de pago, detalles de tarjeta,
detalles de wallet, clasificación e información general. Las reglas de qué métodos se
permiten viven en `PaymentMethodPolicy`; la sección muestra esas opciones y limpia los
campos que dejan de aplicar al cambiar la selección. Al editar se carga la transacción
completa con `GET_TRANSACTION`. El detalle de reglas está en el agente client.

## Pruebas

| Prueba | Ubicación | Requiere |
| --- | --- | --- |
| `*FunctionalTest`, `*GuiFunctionalTest`, `CardCreationTest` | `src/main/java/.../<feature>/test/` | Servidor en ejecución |
| `TestTable` | `src/test/java` | Nada; muestra la tabla genérica en una ventana |

Son programas `main` que se ejecutan desde el IDE.
