# Crear una feature en shared

[Arquitectura](../../../../../../../ARCHITECTURE.md) ·
[Estado](../../../../../../../MIGRATION.md)

Shared publica modelos y contratos entre el servidor y el cliente mediante sockets y
JSON. No implementa casos de uso, persistencia ni UI. Cada feature migrada usa:

```text
<feature>/
├── domain/entities/<Entity>.java
├── domain/enums/<Enum>.java                         # si aplica
├── domain/exceptions/<Entity>ValidationException.java # si hay regla de dominio
├── application/exceptions/<Entity><Operation>Exception.java
└── infrastructure/serialization/<Entity>Mapper.java
```

Cree únicamente las carpetas necesarias. `tags` es la primera referencia migrada:
`Tag` conserva sus propiedades y `TagMapper.toMap/fromMap` convierte el payload.
Las excepciones de creación, lectura, actualización, borrado y ausencia están en
archivos separados de `application/exceptions`.

El mapper escribe exactamente los nombres camelCase del protocolo existente. Convierte
fechas con `ValueParser` y códigos de enum con `getValue`/`fromValue`. No agregue reglas
financieras al mapper. `Transaction` es la raíz del agregado y compone los mappers de
los detalles sin duplicar sus claves. Los códigos de enum deben coincidir con los CHECK
vigentes de la base de datos; sus etiquetas españolas se usan en la UI.

Al migrar una feature, actualice los imports del backend y client en el mismo cambio,
regenere índices con `python3 scripts/update_indexes.py`, compare los contratos con
`python3 scripts/verify_shared.py`, y actualice `MIGRATION.md` y el agente de shared.
`MIGRATION.md` indica el estado de backend y client.
