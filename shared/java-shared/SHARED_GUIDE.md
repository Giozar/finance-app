# Guía de shared

[Arquitectura común](../../ARCHITECTURE.md) · [Árbol de shared](SHARED_ARCHITECTURE.md)

## Para qué sirve

`java-shared` es el JAR que comparten `java-server` y `java-client`. Define las
entidades, enums, errores y conversiones que ambos usan para intercambiar datos.
También contiene el mensaje del protocolo, su codificación JSON, el parseo de
valores y el logger de consola. No contiene pantallas, acceso a MySQL ni casos de uso.

Las features actuales incluyen `users`, `accounts`, `accountCashbackSettings`,
`accountReconciliations`, `bankClient`, `card`, `cardTransactionDetails`,
`walletCardLinks`, `walletTransactionDetails`, `categories`, `tags`,
`externalEntities` y `transactions`. `Transaction` es la raíz del agregado: incluye
`tagIds` y los detalles de tarjeta o wallet que correspondan.

## Cómo está construido

Cada feature vive en `src/main/java/com/giozar04/<feature>/` y crea solo las capas
que necesita:

```text
<feature>/
├── domain/
│   ├── entities/<Entity>.java
│   ├── enums/<Enum>.java                         # si aplica
│   └── exceptions/<Entity>ValidationException.java # si hay regla de dominio
├── application/exceptions/<Entity><Operation>Exception.java
└── infrastructure/serialization/
    ├── <Entity>Mapper.java
    └── <Entity>ParsingException.java              # si aplica
```

Las piezas compartidas entre features están en:

```text
messages/infrastructure/transport/Message.java
messages/infrastructure/serialization/MessageJsonCodec.java
shared/infrastructure/serialization/ValueParser.java
logging/infrastructure/ConsoleLogger.java
```

El servidor y el cliente convierten entidades con `<Entity>Mapper.toMap` y
`fromMap`. `MessageJsonCodec` convierte el mensaje completo a JSON y lo lee de
vuelta. El mapper conserva nombres de campo en camelCase, nulos, valores por
defecto, tipos numéricos y fechas del protocolo. `ValueParser` centraliza el
parseo de valores escalares. `tags/TagMapper` muestra el caso sencillo;
`transactions/TransactionMapper` muestra un agregado con detalles anidados.

Los enums exponen `getValue()` para el código persistido y `getLabel()` para la
interfaz. `fromValue(...)` interpreta el código recibido. Los valores que se
guardan en MySQL deben coincidir con los `CHECK` del esquema. Los errores de
operación van en archivos separados de `application/exceptions`; los errores
propios de interpretar mapas van en `infrastructure/serialization`.

## Cómo implementar o ampliar una feature

1. Revise una feature comparable en [el árbol](SHARED_ARCHITECTURE.md) y defina
   qué datos deben compartir servidor y cliente.
2. Cree o amplíe la entidad y sus enums bajo `domain`. Añada solo las excepciones
   que requieran sus operaciones o reglas.
3. Implemente `toMap` y `fromMap` en el mapper de la feature. Conserve las claves,
   valores por defecto y formatos que esperan ambos extremos. Para datos anidados,
   componga los mappers correspondientes.
4. Actualice los consumidores de backend y client cuando cambie una propiedad,
   un enum o la API Java. Revise los tipos de mensaje y las columnas de MySQL si
   ese dato también se persiste.
5. Actualice `SHARED_ARCHITECTURE.md` si cambian las rutas y compile los módulos
   afectados mediante Maven para comprobar sus contratos.

La compilación del proyecto completo y la dependencia Maven de shared están
documentadas en el [README del proyecto](../../README.md).
