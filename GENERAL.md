# Índice del proyecto

[Arquitectura](ARCHITECTURE.md) · [Estado de migración](MIGRATION.md) · [Instrucciones](AGENTS.md)

| Módulo | Índice | Agente |
| --- | --- | --- |
| Shared | [GENERALSHARED.md](shared/java-shared/GENERALSHARED.md) | [shared](.claude/agents/finance-app-expert-shared.md) |
| Backend | [GENERALBACKEND.md](backend/java-server/GENERALBACKEND.md) | [backend](.claude/agents/finance-app-expert-backend.md) |
| Client | [GENERALCLIENT.md](client/java-client/GENERALCLIENT.md) | [client](.claude/agents/finance-app-expert-client.md) |
| Database | `database/schemas.sql` y `database/migrations/` | [database](.claude/agents/finance-app-expert-database.md) |
| Git | Historial y ramas | [git](.claude/agents/finance-app-expert-git.md) |

Los índices se regeneran con `python3 scripts/update_indexes.py`.
Verificación de shared y sus consumidores: `python3 scripts/verify_shared.py`.
