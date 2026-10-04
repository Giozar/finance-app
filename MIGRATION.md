# Seguimiento de Clean Architecture

Rama de trabajo: `refactor/clean-architecture`.
Referencia funcional inicial: `a68517e`.

## Shared

| Feature | Estado |
| --- | --- |
| tags | Migrada; contratos y consumidores verificados |
| users | Migrada; contratos y consumidores verificados |
| categories | Migrada; contratos y consumidores verificados |
| externalEntities | Pendiente |
| bankClient | Pendiente |
| accounts | Pendiente |
| card | Pendiente |
| accountCashbackSettings | Pendiente |
| walletCardLinks | Pendiente |
| cardTransactionDetails | Pendiente |
| walletTransactionDetails | Pendiente |
| transactions | Pendiente |
| accountReconciliations | Pendiente |
| serialización, mensajes y logging | Pendiente |

Cada fila completada incluye referencias de consumidores, documentación, contratos
comparados y compilación independiente de shared, backend y client.

## Siguientes etapas

- Backend: diagnóstico de dependencias → piloto tags → features según dependencias → transactions/conciliación.
- Client: diagnóstico de dependencias → piloto tags → catálogos/cuentas → transactions.

La compilación de consumidores durante shared no representa la migración interna de backend/client.
