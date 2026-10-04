# Guía de shared

[Arquitectura común](../../ARCHITECTURE.md) · [Árbol de shared](SHARED_ARCHITECTURE.md) · [Agente](../../.claude/agents/finance-app-expert-shared.md)

## Para qué sirve

`java-shared` es el JAR que comparten `java-server` y `java-client`. Define las
entidades, enums, errores y conversiones que ambos usan para intercambiar datos.
También contiene el mensaje del protocolo, su codificación JSON, el parseo de
valores y el logger de consola. No contiene pantallas, acceso a MySQL, puertos ni
casos de uso.

Maven: `com.giozar04:java-shared:1.0-SNAPSHOT`, empaquetado `jar`, Java 17 y sin
dependencias externas.

Features: `users`, `accounts`, `accountCashbackSettings`, `accountReconciliations`,
`bankClient`, `card`, `cardTransactionDetails`, `walletCardLinks`,
`walletTransactionDetails`, `categories`, `tags`, `externalEntities` y
`transactions`. `Transaction` es la raíz del agregado: incluye `tagIds` y los
detalles de tarjeta o wallet que correspondan.

Los paquetes `card` y `bankClient` están en singular; en backend y client las mismas
features se llaman `cards` y `bankClients`. Los nombres se conservan.

## Cómo está construido

Cada feature vive en `src/main/java/com/giozar04/<feature>/` y crea solo las capas
que necesita:

```text
<feature>/
├── domain/
│   ├── entities/<Entity>.java
│   ├── enums/<Enum>.java                           # si aplica
│   └── exceptions/<Entity>ValidationException.java # si hay regla de dominio
├── application/exceptions/<Entity><Operation>Exception.java
└── infrastructure/serialization/
    ├── <Entity>Mapper.java
    └── <Entity>ParsingException.java               # si aplica
```

Las piezas compartidas entre features son:

| Clase | Responsabilidad |
| --- | --- |
| `messages/infrastructure/transport/Message.java` | Mensaje del protocolo: `type`, `content`, `data` y `status` (`SUCCESS`, `ERROR`, `PENDING`) |
| `messages/infrastructure/serialization/MessageJsonCodec.java` | `encode` / `decode` del mensaje completo a JSON |
| `shared/infrastructure/serialization/ValueParser.java` | Parseo de números, `BigDecimal`, fechas y su formato |
| `logging/infrastructure/ConsoleLogger.java` | Logger de consola (`getInstance()`) |

## Contrato del protocolo

Backend y client intercambian entidades como mapas dentro de `Message.data`:

```text
Entidad ──<Entity>Mapper.toMap──▶ Map ──MessageJsonCodec.encode──▶ JSON por socket
JSON ──MessageJsonCodec.decode──▶ Map ──<Entity>Mapper.fromMap──▶ Entidad
```

- Las claves del mapa son los nombres de propiedad en camelCase.
- `MessageJsonCodec` escapa `\`, `"`, `\n`, `\r`, `\t` y caracteres de control. Al leer,
  los escalares llegan como `String` y el literal `null` llega como clave ausente.
- Por eso `fromMap` interpreta los valores con `ValueParser` y acepta `String` o
  `Number`. Las variantes `parseNullable*` devuelven `null`; `parseLong` y `parseDouble`
  devuelven `0`.
- Las fechas usan `ISO_ZONED_DATE_TIME` (`ValueParser.getFormatter()`). Una fecha
  ausente o inválida se convierte en la hora actual.
- Los datos anidados se componen con el mapper correspondiente; por ejemplo,
  `TransactionMapper` usa los mappers de los detalles de tarjeta y de wallet.

Los enums exponen `getValue()` para el código persistido y `getLabel()` para la
interfaz, y `fromValue(...)` interpreta el código recibido. Los códigos están en
mayúsculas, coinciden con el nombre de la constante y deben coincidir con los
`CHECK` del esquema de MySQL.

Los errores de operación van en archivos separados de `application/exceptions`; los
errores propios de interpretar mapas van en `infrastructure/serialization`. Las
excepciones extienden `RuntimeException` y ofrecen los constructores `(String)` y
`(String, Throwable)`.

`tags/TagMapper` muestra el caso sencillo, `accounts` uno con enums y
`transactions/TransactionMapper` un agregado con detalles anidados.

## Pruebas del contrato

`src/test/java/com/giozar04/contracts/ContractProbe.java` serializa un ejemplo de
cada feature (completo, con valores por defecto, ida y vuelta por mapa y por JSON, y
cada valor de sus enums). La salida debe coincidir con la línea base
`src/test/resources/contracts.json`. El comando para ejecutarla y compararla está en
[AGENTS.md](../../AGENTS.md#verificación).

Si una tarea cambia el contrato de forma intencionada, actualice `contracts.json` en
el mismo cambio. Si añade una feature, regístrela en la lista `FEATURES` del probe.

## Cómo implementar o ampliar una feature

1. Revise una feature comparable en [el árbol](SHARED_ARCHITECTURE.md) y defina qué
   datos deben compartir servidor y cliente.
2. Cree o amplíe la entidad y sus enums bajo `domain`. Añada solo las excepciones que
   requieran sus operaciones o reglas.
3. Implemente `toMap` y `fromMap` en el mapper de la feature. Conserve las claves,
   valores por defecto y formatos que esperan ambos extremos.
4. Si el dato se persiste, compruebe la columna y el `CHECK` en `database/schemas.sql`
   con el agente database.
5. Compile shared con `mvn clean install` y ejecute `ContractProbe`.
6. Actualice los consumidores de backend y client cuando cambie una propiedad, un enum
   o la API Java, y recompílelos.
7. Actualice `SHARED_ARCHITECTURE.md` si cambian las rutas y el agente shared si
   cambian contratos o casos particulares.

La compilación del proyecto completo está en el [README](../../README.md#3-compilar).
