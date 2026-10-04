# Arquitectura del proyecto

Estándar común de shared, backend y client. Las guías de cada módulo explican cómo
aplicarlo y sus mapas muestran los archivos actuales:
[shared](shared/java-shared/SHARED_GUIDE.md) ([mapa](shared/java-shared/SHARED_ARCHITECTURE.md)),
[backend](backend/java-server/BACKEND_GUIDE.md) ([mapa](backend/java-server/BACKEND_ARCHITECTURE.md)) y
[client](client/java-client/CLIENT_GUIDE.md) ([mapa](client/java-client/CLIENT_ARCHITECTURE.md)).

## Organización

El código de cada módulo vive en `src/main/java/com/giozar04/`, se organiza por feature
y, dentro de cada feature, por capas. Cada feature crea solo las capas que necesita.

```text
                 Swing (client)          socket (backend)
                       │                       │
presentation ──▶ application ◀── infrastructure ──▶ MySQL / socket / JSON
                       │
                       ▼
                    domain        (entidades y enums viven en shared)
```

Las flechas indican dependencia. Ninguna capa interior conoce a una exterior; los
adaptadores de `infrastructure` implementan los puertos de `application`, y
`bootstrap` los conecta.

| Capa | Shared | Backend | Client |
| --- | --- | --- | --- |
| `domain` | Entidades, enums y excepciones de reglas | Políticas (`domain/policies`) sobre las entidades de shared | — (usa las de shared) |
| `application` | Excepciones de operaciones | Puertos, casos de uso, normalización y validación | Puertos, casos de uso y `ClientOperationException` |
| `infrastructure` | Mappers, mensaje, codec JSON, `ValueParser`, logger | Repositorios MySQL, controllers y handlers socket | Servicios socket (gateways) y conexión al servidor |
| `presentation` | — | — | Vistas, formularios y componentes Swing |
| `bootstrap` | — | `ApplicationInitializer` y arranque del servidor | `ApplicationInitializer` y `ClientUseCases` |

## Regla de dependencias

- `domain`: entidades, enums, excepciones y políticas de reglas puras. Depende del JDK
  y del dominio de otras features cuando existe una relación explícita, como los
  detalles de una transacción.
- `application`: casos de uso, puertos de entrada (`*Operations`) y salida
  (`*Repository` en backend, `*Gateway` en client) y excepciones de operaciones.
  Depende del dominio; no importa JDBC, Swing, sockets, JSON, mappers ni
  implementaciones de logging.
- `infrastructure`: implementaciones de persistencia, transporte, serialización y
  logging. Implementa los puertos de `application` y usa los mappers de shared.
- `presentation` (client): vistas, componentes y coordinación de la interacción.
  Obtiene los puertos de entrada con `ClientUseCases.get(<Feature>Operations.class)`;
  no construye conexiones, mensajes de red ni servicios socket.
- `bootstrap`: composition root. Es el único lugar que instancia adaptadores concretos
  y los entrega a los casos de uso.

Shared es una biblioteca de modelos y contratos: no tiene puertos, repositorios ni
casos de uso, y sus mappers son adaptadores de serialización. No se crean capas
vacías para cumplir la plantilla.

## Vocabulario y ubicaciones

Todas las rutas son relativas a `src/main/java/com/giozar04/`.

### Shared

| Responsabilidad | Ubicación y nombre |
| --- | --- |
| Modelo compartido | `<feature>/domain/entities/<Entity>.java` |
| Valores controlados | `<feature>/domain/enums/<Enum>.java` (`getValue`, `getLabel`, `fromValue`) |
| Error de regla del dominio | `<feature>/domain/exceptions/<Entity>ValidationException.java` |
| Fallo de una operación | `<feature>/application/exceptions/<Entity><Operation>Exception.java` |
| Conversión entidad/mapa | `<feature>/infrastructure/serialization/<Entity>Mapper.java`, `toMap` / `fromMap` |
| Error al interpretar un payload | `<feature>/infrastructure/serialization/<Entity>ParsingException.java` |
| Mensaje del protocolo | `messages/infrastructure/transport/Message.java` |
| Codificación JSON del mensaje | `messages/infrastructure/serialization/MessageJsonCodec.java`, `encode` / `decode` |
| Conversión de valores escalares | `shared/infrastructure/serialization/ValueParser.java` |
| Logger de consola | `logging/infrastructure/ConsoleLogger.java` |

### Backend

| Responsabilidad | Ubicación y nombre |
| --- | --- |
| Regla pura de la feature | `<feature>/domain/policies/<Entity>Policy.java` |
| Puerto de entrada | `<feature>/application/ports/input/<Entity>Operations.java` |
| Puerto de persistencia | `<feature>/application/ports/output/<Entity>Repository.java` |
| Caso de uso | `<feature>/application/usecases/<Entity>UseCase.java` (implementa `*Operations`) |
| Código JDBC común | `<feature>/infrastructure/persistence/mysql/Abstract<Entity>JdbcRepository.java` |
| Adaptador MySQL | `<feature>/infrastructure/persistence/mysql/<Entity>RepositoryMySQL.java` |
| Operaciones sobre una conexión compartida | `<feature>/infrastructure/persistence/mysql/<Entity>JdbcOperations.java` |
| Tipos de mensaje y controllers | `<feature>/infrastructure/transport/socket/<Entity>Controllers.java` (clase interna `*MessageTypes`) |
| Registro de handlers | `<feature>/infrastructure/transport/socket/<Entity>Handlers.java` |
| SQL de referencia de la feature | `<feature>/sql/<feature>.sql` (documentación; la fuente de verdad es `database/schemas.sql`) |
| Conexión y unidad de trabajo | `databases/infrastructure/persistence/mysql/` (`TransactionalExecutor`, `SqlWork`) |
| Servidor de sockets | `servers/infrastructure/transport/socket/` |

### Client

| Responsabilidad | Ubicación y nombre |
| --- | --- |
| Puerto de entrada | `<feature>/application/ports/input/<Entity>Operations.java` |
| Puerto hacia el servidor | `<feature>/application/ports/output/<Entity>Gateway.java` |
| Caso de uso | `<feature>/application/usecases/<Entity>UseCase.java` (implementa `*Operations`) |
| Adaptador socket | `<feature>/infrastructure/transport/socket/<Entity>Service.java` (implementa `*Gateway`) |
| Pantallas | `<feature>/presentation/views/` |
| Formularios y paneles de la feature | `<feature>/presentation/components/` |
| Componentes reutilizables del cliente | `shared/components/`, `shared/layouts/`, `shared/utils/` (no es el módulo shared) |
| Conexión con el servidor | `serverConnection/infrastructure/transport/socket/` |

### Reglas de nombres

Cada excepción nueva tiene su archivo y un nombre que identifica su feature. No se
añaden contenedores `*Exceptions` ni utilidades genéricas para responsabilidades
nuevas. Los nombres existentes de paquetes, entidades, propiedades, enums y códigos del
protocolo se conservan para mantener compatibilidad con sus consumidores, incluidos
los paquetes `card` y `bankClient` de shared frente a `cards` y `bankClients` de
backend y client.

## Pruebas

| Tipo | Ubicación | Requiere |
| --- | --- | --- |
| Contrato del protocolo | `shared/java-shared/src/test` (`ContractProbe` y `contracts.json`) | Nada |
| Casos de uso con puertos simulados | `backend/java-server/src/test` (`*UseCaseProbe`) | Nada |
| Pruebas manuales | `<feature>/test/` dentro de `src/main` (`*TestApp`, `*FunctionalTest`) | MySQL o servidor en ejecución |

Las pruebas son programas `main` sin JUnit. Los comandos están en
[AGENTS.md](AGENTS.md#verificación). Las pruebas manuales construyen adaptadores
concretos por sí mismas, como un composition root reducido.

## Compatibilidad

Los cambios de API de shared se coordinan con sus referencias en backend y client.
La compatibilidad del sistema se basa en los contratos del protocolo JSON y en que
los módulos se compilen juntos; no implica compatibilidad binaria con JAR antiguos.

Se conservan campos camelCase, códigos de enums, nulls, valores por defecto, tipos
numéricos, escalas, fechas y mensajes de error. Las reglas de saldos se implementan
en los triggers y procedimientos existentes de MySQL; solo las transacciones
`COMPLETED` afectan saldos.

La conversión compartida de fechas ausentes o inválidas devuelve la hora actual; los
consumidores deben tener en cuenta ese comportamiento.

## Excepciones conocidas al estándar

Son código heredado que se conserva hasta que una tarea lo cambie de forma explícita.
No deben tomarse como modelo:

- `databases/.../DatabaseExceptions.java` (backend) agrupa varias excepciones en un
  contenedor.
- Las clases internas de tipos de mensaje se llaman `MessageTypes` en
  `accountCashbackSettings`, `cardTransactionDetails` y `walletTransactionDetails`, y
  `<Entity>MessageTypes` en las demás features. Las nuevas usan `<Entity>MessageTypes`.
- El cliente tiene dos clases `ServerConnectionConfig`: `configs/` lee host y puerto de
  `config.properties`; `serverConnection/.../socket/` guarda el estado de la conexión.
- `transactions/presentation` (client) usa `ConsoleLogger` directamente.
- Las pruebas manuales de `<feature>/test/` se compilan junto con el código de producción.
- `shared/java-shared/.../Main.java` es una clase de ejemplo sin uso.

## Documentación y agentes

- Este archivo define el estándar común.
- [AGENTS.md](AGENTS.md) centraliza el flujo de trabajo, la verificación y qué
  documentación actualizar.
- [PROJECT_MAP.md](PROJECT_MAP.md) enlaza las guías, mapas y agentes de cada módulo.
- `*_GUIDE.md` explica el propósito, la organización y cómo implementar cambios en
  cada módulo; `*_ARCHITECTURE.md` muestra su árbol de archivos.
- Los agentes de `.claude/agents/` conservan el contexto específico de cada módulo:
  contratos, mensajes, reglas y casos particulares.
