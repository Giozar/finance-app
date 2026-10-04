# Arquitectura del proyecto

## Organización

El código se organiza por feature y separa dominio, aplicación, infraestructura y,
en el cliente, presentación. Los mapas de [shared](shared/java-shared/SHARED_ARCHITECTURE.md),
[backend](backend/java-server/BACKEND_ARCHITECTURE.md) y
[client](client/java-client/CLIENT_ARCHITECTURE.md) muestran los archivos actuales.

## Regla de dependencias

- `domain`: entidades, enums y excepciones de reglas del dominio. Depende del JDK y
  del dominio de otras features cuando existe una relación explícita, como los detalles de una transacción.
- `application`: casos de uso, contratos de entrada y salida y excepciones de operaciones.
  Depende del dominio; no importa JDBC, Swing, sockets, JSON ni implementaciones de logging.
- `infrastructure`: implementaciones de persistencia, transporte, serialización y logging.
  Depende de los contratos interiores.
- `presentation` (client): vistas, componentes y coordinación de la interacción.
  Consume los casos de uso; no construye conexiones ni mensajes de red.
- `bootstrap`: composition root; construye y conecta las implementaciones.

Shared es una biblioteca de modelos y contratos. Sus mappers son adaptadores de
serialización, no casos de uso. No se crean repositorios ni casos de uso vacíos en shared.
Backend y client separan puertos, casos de uso y adaptadores por feature.

## Vocabulario y ubicaciones

| Responsabilidad | Ubicación y nombre |
| --- | --- |
| Modelo compartido | `<feature>/domain/entities/<Entity>.java` |
| Valores controlados | `<feature>/domain/enums/<Enum>.java` |
| Error de regla del dominio | `<feature>/domain/exceptions/<Entity>ValidationException.java` |
| Fallo de una operación | `<feature>/application/exceptions/<Entity><Operation>Exception.java` |
| Conversión entidad/mapa | `<feature>/infrastructure/serialization/<Entity>Mapper.java`, `toMap` / `fromMap` |
| Error al interpretar un payload | `<feature>/infrastructure/serialization/<Entity>ParsingException.java` |
| Mensaje del protocolo | `messages/infrastructure/transport/Message.java` |
| Codificación JSON del mensaje | `messages/infrastructure/serialization/MessageJsonCodec.java`, `encode` / `decode` |
| Conversión de valores escalares del protocolo | `shared/infrastructure/serialization/ValueParser.java` |
| Logger de consola | `logging/infrastructure/ConsoleLogger.java` |
| Caso de uso (backend/client) | `<feature>/application/usecases/` |
| Puerto (backend/client) | `<feature>/application/ports/input/` o `output/` |

Cada excepción nueva tiene su archivo y un nombre que identifica su feature. No se
añaden contenedores `*Exceptions` ni utilidades genéricas para responsabilidades nuevas.
Los nombres existentes de paquetes, entidades, propiedades, enums y códigos del protocolo
se conservan para mantener compatibilidad con sus consumidores.

## Compatibilidad

Los cambios de API de shared se coordinan con sus referencias en backend y client.
La compatibilidad del sistema se basa en los contratos del protocolo JSON y en que
los módulos se compilen juntos; no implica compatibilidad binaria con JAR antiguos.

Se conservan campos camelCase, códigos de enums, nulls, valores por defecto, tipos
numéricos, escalas, fechas y mensajes de error. Las reglas de saldos se implementan
en los triggers y procedimientos existentes.

La conversión compartida de fechas ausentes o inválidas devuelve la hora actual; los
consumidores deben tener en cuenta ese comportamiento.

## Documentación y agentes

- Este archivo define el estándar común.
- `PROJECT_MAP.md` enlaza los mapas de arquitectura de cada módulo.
- `*_GUIDE.md` explica el propósito, la organización y cómo implementar cambios en cada módulo.
- [AGENTS.md](AGENTS.md) centraliza el flujo de trabajo.
- Los agentes de `.claude/agents/` conservan el contexto específico de cada módulo y
  enlazan este estándar y sus mapas de archivos.
