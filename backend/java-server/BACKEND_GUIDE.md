# Guía de backend

[Arquitectura común](../../ARCHITECTURE.md) · [Árbol de backend](BACKEND_ARCHITECTURE.md) · [Agente](../../.claude/agents/finance-app-expert-backend.md)

## Para qué sirve

`java-server` recibe las solicitudes del cliente por sockets, ejecuta las operaciones
de cada feature y persiste los datos en MySQL. Usa las entidades, excepciones,
mappers y mensajes de `java-shared`. Los tipos de mensaje y las claves de sus datos
forman parte del contrato con el cliente.

Dependencias Maven: `java-shared` y `mysql-connector-java` 8.0.33. La configuración
se lee de `src/main/resources/config.properties` (plantilla:
`config.example.properties`; véase el [README](../../README.md#2-configuración)).

## Cómo está construido

El código vive en `src/main/java/com/giozar04/`:

| Paquete | Responsabilidad |
| --- | --- |
| `Main` | Llama a `bootstrap/ApplicationInitializer.start()` |
| `bootstrap/` | `ApplicationInitializer` (composition root), `DatabaseInitializer` y `ServerInitializer` |
| `configs/` | `AppConfig` lee `config.properties`; `DatabaseConfig` y `ServerConfig` exponen sus valores |
| `databases/infrastructure/persistence/mysql/` | Conexión JDBC, `TransactionalExecutor`, `SqlWork` y `DatabaseExceptions` |
| `servers/infrastructure/transport/socket/` | `ServerService`, `ClientConnection`, `MessageHandler` y `ServerRegisterHandlers` |
| `<feature>/` | Una carpeta por feature con las capas que usa |

Cada feature tiene esta estructura:

```text
<feature>/
├── domain/policies/<Entity>Policy.java              # reglas sin JDBC ni sockets
├── application/
│   ├── ports/input/<Entity>Operations.java          # operaciones que ofrece la feature
│   ├── ports/output/<Entity>Repository.java         # persistencia que necesita
│   └── usecases/<Entity>UseCase.java                # implementa Operations
├── infrastructure/
│   ├── persistence/mysql/
│   │   ├── Abstract<Entity>JdbcRepository.java      # código común y validación con la política
│   │   ├── <Entity>JdbcOperations.java              # si participa en una unidad de trabajo
│   │   └── <Entity>RepositoryMySQL.java             # implementa Repository
│   └── transport/socket/
│       ├── <Entity>Controllers.java                 # <Entity>MessageTypes y un controller por mensaje
│       └── <Entity>Handlers.java                    # registra los controllers en el servidor
├── sql/<feature>.sql                                # SQL de referencia (no se ejecuta)
└── test/<Entity>TestApp.java                        # prueba manual contra MySQL
```

`transactions` añade `application/normalizers` y `application/validation` (reglas por
estrategia). `transactionTags` solo tiene persistencia: escribe `transaction_tags`
para el agregado de transacciones.

## Recorrido de una solicitud

```text
cliente ──JSON (una línea)──▶ ServerService ──type──▶ MessageHandler (controller)
   controller: lee message.getData(...) ──Mapper.fromMap──▶ entidad
            ──▶ <Entity>Operations ──▶ <Entity>UseCase ──▶ <Entity>Policy
            ──▶ <Entity>Repository ──▶ <Entity>RepositoryMySQL ──▶ MySQL
   respuesta: Message.createSuccessMessage(type, texto) + addData(clave, Mapper.toMap(...))
```

- Al conectarse, el servidor envía un mensaje `WELCOME`. Cada solicitud y respuesta es
  un `Message` codificado con `MessageJsonCodec` en una sola línea.
- `ServerService` busca el handler por `type`. Si no existe responde
  "Tipo de mensaje no soportado: <tipo>".
- El controller valida la presencia de los datos (`"Datos no proporcionados"`,
  `"ID inválido"`) y responde con `Message.createErrorMessage`.
- Si el caso de uso o el repositorio lanzan una excepción, `ServerService` responde un
  error con "Error al procesar solicitud: <mensaje>". Por eso los mensajes de las
  excepciones llegan al usuario y deben estar redactados para él.
- Las respuestas de listas usan la clave plural de la entidad y `"count"`; las de un
  elemento, la clave singular (`"tag"`, `"tags"`).

## Persistencia y unidad de trabajo

- Los repositorios usan `DatabaseConnectionInterface.getConnection()` (conexión
  compartida) para operaciones de una tabla.
- Las escrituras que abarcan varias tablas usan
  `TransactionalExecutor.inTransaction(SqlWork<T>)`: abre una conexión dedicada con
  `createConnection()`, hace commit, o rollback ante cualquier excepción, y la cierra.
- Los repositorios que participan reciben la `Connection` mediante su interfaz
  `*JdbcOperations` y no hacen commit, rollback ni close.
- Los triggers y procedimientos de MySQL aplican los efectos sobre saldos. El backend
  no recalcula saldos.

`tags` es la referencia sencilla: `TagUseCase` recibe `TagRepository` y aplica
`TagPolicy` antes de escribir. `transactions` es el agregado más complejo: normaliza,
valida y luego guarda la transacción, sus detalles y etiquetas en una sola unidad de
trabajo; el orden de escritura es parte del comportamiento (véase el agente).

## Cómo implementar o ampliar una feature

1. Consulte [el árbol](BACKEND_ARCHITECTURE.md) y una feature comparable. Confirme que
   las entidades y el mapper necesarios existen en shared.
2. Defina la operación en `*Operations` y los accesos a datos en `*Repository`.
3. Implemente el caso de uso. Coloque las reglas puras en la política de dominio y
   mantenga la orquestación fuera de los controllers y del SQL.
4. Implemente el adaptador MySQL con sus consultas y mapeo de resultados. Para
   escrituras sobre varias tablas, use `TransactionalExecutor` y las interfaces
   `*JdbcOperations` de los repositorios participantes.
5. Añada el tipo de mensaje en `<Entity>MessageTypes`, el controller y su registro en
   `<Entity>Handlers`. Use los códigos y claves que espera el cliente.
6. En `ApplicationInitializer`, construya el repositorio y el caso de uso, y añada el
   handler a la lista de registradores.
7. Si cambia el esquema, coordínelo con el agente database: `database/schemas.sql` y
   una migración en `database/migrations/`. Actualice el `sql/<feature>.sql` de
   referencia. No ejecute `schemas.sql` sobre una base existente: la recrea.
8. Compile, ejecute los probes (véase [AGENTS.md](../../AGENTS.md#verificación)) y,
   si dispone de MySQL, el `TestApp` de la feature.
9. Actualice `BACKEND_ARCHITECTURE.md`, este documento si cambia el flujo, el agente
   backend y los consumidores del cliente.

Mantenga los códigos de mensaje, valores por defecto, mensajes de error y reglas de
rollback. El [agente backend](../../.claude/agents/finance-app-expert-backend.md)
documenta los mensajes de cada feature y los casos de transacciones y conciliación.

## Pruebas

| Prueba | Ubicación | Cómo se ejecuta |
| --- | --- | --- |
| `TagUseCaseProbe`, `TransactionUseCaseProbe` | `src/test/java` | Con `java -cp`, sin base de datos (comando en AGENTS.md) |
| `<Entity>TestApp` | `src/main/java/.../<feature>/test/` | Desde el IDE, con MySQL configurado; son menús de consola que escriben datos reales |
