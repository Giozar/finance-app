# Guía de backend

[Arquitectura común](../../ARCHITECTURE.md) · [Árbol de backend](BACKEND_ARCHITECTURE.md)

## Para qué sirve

`java-server` recibe las solicitudes del cliente por sockets, ejecuta las
operaciones de cada feature y persiste los datos en MySQL. Usa las entidades,
excepciones, mappers y mensajes de `java-shared`. El protocolo transporta mensajes
JSON; los tipos de mensaje y las claves de sus datos forman parte del contrato con
el cliente.

## Cómo está construido

El código vive en `src/main/java/com/giozar04/`. `bootstrap/ApplicationInitializer`
construye repositorios, casos de uso y handlers; `ServerInitializer` inicia el
servidor y `DatabaseInitializer` prepara la conexión. Los componentes transversales
de JDBC están en `databases/infrastructure/persistence/mysql`, y el servidor de
sockets está en `servers/infrastructure/transport/socket`.

Cada feature tiene esta estructura, según las responsabilidades que usa:

```text
<feature>/
├── domain/policies/<Feature>Policy.java               # reglas sin JDBC
├── application/
│   ├── ports/input/<Feature>Operations.java           # operaciones solicitables
│   ├── ports/output/<Feature>Repository.java          # persistencia requerida
│   └── usecases/<Feature>UseCase.java
└── infrastructure/
    ├── persistence/mysql/
    │   ├── Abstract<Feature>JdbcRepository.java      # código común, si existe
    │   └── <Feature>RepositoryMySQL.java
    └── transport/socket/
        ├── <Feature>Controllers.java
        └── <Feature>Handlers.java
```

Una solicitud sigue este recorrido:

```text
Message JSON → ServerService → Handler → Controller → Operations
             → UseCase → Repository → RepositoryMySQL → MySQL
```

El handler registra el tipo de mensaje. El controller lee sus datos y convierte
entidades mediante los mappers de shared. El caso de uso depende del puerto de
salida; el adaptador MySQL implementa ese puerto. Las políticas de dominio no
dependen de sockets, JSON ni JDBC. Las respuestas se convierten de nuevo a
`Message` con las claves que espera el cliente.

`tags` es una referencia sencilla: `TagUseCase` recibe `TagRepository` y aplica
`TagPolicy` antes de escribir. `transactions` es el agregado más complejo: primero
normaliza y valida, luego guarda la transacción, sus detalles y etiquetas mediante
`TransactionalExecutor`. Los repositorios participantes usan la misma conexión.
Los triggers y procedimientos de MySQL aplican los efectos sobre saldos; el orden
de escritura de detalles y etiquetas es parte del comportamiento actual.

## Cómo implementar o ampliar una feature

1. Consulte [el árbol](BACKEND_ARCHITECTURE.md) y una feature comparable. Confirme
   que las entidades y el mapper necesarios existen en shared.
2. Defina la operación en el puerto de entrada (`*Operations`) y los accesos a datos
   que requiere en el puerto de salida (`*Repository`).
3. Implemente el caso de uso. Coloque reglas puras en una política de dominio y
   mantenga la orquestación fuera de los controllers y del SQL.
4. Implemente el adaptador MySQL con sus consultas y mapeo de resultados. Para
   escrituras que abarcan varias tablas, utilice `TransactionalExecutor` y los
   contratos JDBC de los repositorios participantes.
5. Añada el controller y el handler socket: lea los campos del mensaje, invoque el
   puerto de entrada y forme la respuesta con las claves y los mensajes existentes.
6. Registre el repositorio, caso de uso y handlers en `ApplicationInitializer`.
7. Si cambia el esquema, actualice `database/schemas.sql` y la migración SQL que
   corresponda a bases con datos. No ejecute `schemas.sql` sobre una base existente:
   recrea la base de datos.
8. Actualice `BACKEND_ARCHITECTURE.md`, el agente backend y los consumidores del
   cliente afectados. Compile y ejecute las pruebas Maven relevantes.

Mantenga los códigos de mensaje, valores por defecto, mensajes de error y reglas
de rollback. El [agente backend](../../.claude/agents/finance-app-expert-backend.md)
documenta los casos particulares de transacciones y conciliación.
