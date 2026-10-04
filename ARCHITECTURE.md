# Arquitectura por features

## Objetivo y estado

La migración sigue el orden shared → backend → client y se realiza por feature.
Cada commit actualiza sus consumidores, documentación y agente. El protocolo y los
resultados observables se conservan. El estado real se registra en [MIGRATION.md](MIGRATION.md).

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
Backend ya separa puertos, casos de uso y adaptadores por feature. Client se migra después.

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
Los nombres históricos de paquetes de features se conservan en esta etapa; no se
renombran entidades, propiedades, enums ni códigos del protocolo por razones cosméticas.

## Compatibilidad

Los cambios de API Java de shared se aplican junto con sus referencias en backend y
client en el mismo commit, y ambos se recompilan. La compatibilidad garantizada por
esta etapa es del protocolo JSON y del código del repositorio recompilado; no es una
promesa de compatibilidad binaria con JAR antiguos o serialización nativa de Java.

Se conservan campos camelCase, códigos de enums, nulls, valores por defecto, tipos
numéricos, escalas, fechas y mensajes de error. Las reglas de saldos permanecen en los
triggers y procedimientos existentes durante esta migración estructural.

Los cambios funcionales detectados se registran y se abordan aparte. Por ejemplo,
la conversión actual de una fecha ausente o inválida devuelve la hora actual.

## Verificación

`python3 scripts/verify_shared.py` requiere Python 3 y JDK 17. Compila separadamente
los tres módulos contra el shared del checkout en un directorio temporal y compara
los contratos con `shared/java-shared/src/test/resources/contracts.json`.
La referencia se captura antes de la migración y no se regenera para hacer pasar un refactor.
El probe incluye campos completos, defaults, ida y vuelta por mapas y JSON, todos los
valores de enum, detalles anidados, nulos, etiquetas, caracteres escapados y fechas.

Esta verificación no arranca Swing, sockets ni MySQL. La comprobación funcional con
servidor y base de datos corresponde a las etapas de backend/client y usa datos aislados.

## Documentación y agentes

- Este archivo define el estándar común.
- `GENERAL.md` y los `GENERAL*.md` de los módulos contienen índices generados del código.
- Las guías `*-explanation.md` explican cómo implementar una feature en cada etapa.
- [AGENTS.md](AGENTS.md) centraliza el flujo de trabajo.
- Los agentes de `.claude/agents/` conservan el contexto específico de cada módulo y
  enlazan el estándar; sus instrucciones anteriores se adaptan a la migración vigente.
