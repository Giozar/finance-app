# Mapa del proyecto

[Instrucciones](AGENTS.md) · [Arquitectura común](ARCHITECTURE.md) · [README](README.md)

## Documentos raíz

| Documento | Contenido |
| --- | --- |
| [AGENTS.md](AGENTS.md) | Punto de entrada: qué leer, reglas, verificación, documentación a actualizar y especialistas |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Capas, regla de dependencias, vocabulario, pruebas, compatibilidad y excepciones conocidas |
| [README.md](README.md) | Instalación, base de datos, configuración, compilación y ejecución |

## Módulos

Cada módulo tiene una guía (para qué sirve, cómo está construido y cómo implementar un
cambio), un mapa de arquitectura (árbol de archivos actual) y un agente (contratos,
mensajes y casos particulares).

| Módulo | Guía | Mapa de arquitectura | Agente |
| --- | --- | --- | --- |
| Shared | [SHARED_GUIDE.md](shared/java-shared/SHARED_GUIDE.md) | [SHARED_ARCHITECTURE.md](shared/java-shared/SHARED_ARCHITECTURE.md) | [shared](.claude/agents/finance-app-expert-shared.md) |
| Backend | [BACKEND_GUIDE.md](backend/java-server/BACKEND_GUIDE.md) | [BACKEND_ARCHITECTURE.md](backend/java-server/BACKEND_ARCHITECTURE.md) | [backend](.claude/agents/finance-app-expert-backend.md) |
| Client | [CLIENT_GUIDE.md](client/java-client/CLIENT_GUIDE.md) | [CLIENT_ARCHITECTURE.md](client/java-client/CLIENT_ARCHITECTURE.md) | [client](.claude/agents/finance-app-expert-client.md) |
| Database | [README: base de datos](README.md#1-base-de-datos) | Índice de tablas, triggers y procedimientos en su agente | [database](.claude/agents/finance-app-expert-database.md) |
| Git | [AGENTS.md: commits y ramas](AGENTS.md#commits-y-ramas) | — | [git](.claude/agents/finance-app-expert-git.md) |

## Base de datos

| Archivo | Uso |
| --- | --- |
| `database/schemas.sql` | Esquema completo y fuente de verdad. Recrea la base `finanzas` y borra sus datos |
| `database/migrations/2026-10-03_reallocation.sql` | Operación `REALLOCATION` y métodos de pago controlados |
| `database/migrations/2026-10-03_schema_consistency.sql` | Valores en mayúsculas, `CHECK` de enums y `cashback_rate` como fracción |
| `database/migrations/2026-10-03_transaction_integrity.sql` | Estados de transacción; solo `COMPLETED` afecta saldos |
| `database/migrations/2026-10-04_account_reconciliation.sql` | Saldos de apertura, vista de conciliación y `sp_reconcile_account` |
| `database/migrations/2026-10-05_transactions_redesign.sql` | Reglas del rediseño de transacciones, wallet y tarjetas |

Las migraciones se aplican en orden de fecha, una sola vez, sobre bases con datos.
Su detalle está en el agente database.
