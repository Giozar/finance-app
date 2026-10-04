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
| serialización, mensajes y logging | Migrada; contratos y consumidores verificados |

Cada fila completada incluye referencias de consumidores, documentación, contratos
comparados y compilación independiente de shared, backend y client.

## Backend

| Feature | Estado |
| --- | --- |
| tags | Migrada; puertos, caso de uso, política y adaptadores verificados |
| users | Migrada; puertos, política y adaptadores verificados |
| categories | Migrada; puertos, política y adaptadores verificados |
| externalEntities | Migrada; puertos, política y adaptadores verificados |
| bankClients | Migrada; puertos, política y adaptadores verificados |
| accounts | Migrada; puertos, política y adaptadores verificados |
| cards | Migrada; puertos, política y adaptadores verificados |
| accountCashbackSettings | Migrada; puertos, política y adaptadores verificados |
| walletCardLinks | Migrada; puertos, política y adaptadores verificados |
| cardTransactionDetails | Migrada; puertos, política y adaptadores verificados |
| walletTransactionDetails | Migrada; puertos, política y adaptadores verificados |
| transactions | Migrada; puertos, política y adaptadores verificados |
| accountReconciliations | Migrada; puertos, política y adaptadores verificados |
| transactionTags | Migrada; contrato JDBC dentro de infraestructura |
| databases y servers | Migrados; JDBC y sockets en infraestructura, compilación y contratos verificados |

## Client

| Feature | Estado |
| --- | --- |
| cardTransactionDetails | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| walletCardLinks | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| accountCashbackSettings | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| cards | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| accounts | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| bankClients | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| externalEntities | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| categories | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| users | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |
| tags | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |

La compilación de consumidores durante shared no representa la migración interna de backend/client.
