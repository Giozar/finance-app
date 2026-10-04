# Seguimiento de Clean Architecture

Rama de trabajo: `refactor/clean-architecture`.
Referencia funcional inicial: `a68517e`.

## Shared

| Feature | Estado |
| --- | --- |
| tags | Migrada; contratos y consumidores verificados |
| users | Migrada; contratos y consumidores verificados |
| categories | Migrada; contratos y consumidores verificados |
| externalEntities | Migrada; contratos y consumidores verificados |
| bankClient | Migrada; contratos y consumidores verificados |
| accounts | Migrada; contratos y consumidores verificados |
| card | Migrada; contratos y consumidores verificados |
| accountCashbackSettings | Migrada; contratos y consumidores verificados |
| walletCardLinks | Migrada; contratos y consumidores verificados |
| cardTransactionDetails | Migrada; contratos y consumidores verificados |
| walletTransactionDetails | Migrada; contratos y consumidores verificados |
| transactions | Migrada; contratos y consumidores verificados |
| accountReconciliations | Migrada; contratos y consumidores verificados |
| serialización, mensajes y logging | Pendiente |

Cada fila completada incluye referencias de consumidores, documentación, contratos
comparados y compilación independiente de shared, backend y client.

## Siguientes etapas

- Backend: diagnóstico de dependencias → piloto tags → features según dependencias → transactions/conciliación.
- Client: diagnóstico de dependencias → piloto tags → catálogos/cuentas → transactions.

La compilación de consumidores durante shared no representa la migración interna de backend/client.
