#!/usr/bin/env python3
"""Regenerar los índices de archivos sin leer configuraciones locales."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODULES = (
    ("shared/java-shared", "GENERALSHARED.md", "shared"),
    ("backend/java-server", "GENERALBACKEND.md", "backend"),
    ("client/java-client", "GENERALCLIENT.md", "client"),
)


def update():
    for module, name, agent in MODULES:
        base = ROOT / module
        files = sorted(str(p.relative_to(base)) for p in (base / "src").rglob("*")
                       if p.is_file() and p.suffix in {".java", ".md", ".sql", ".json"})
        content = (f"# Índice de {agent}\n\n"
                   "Generado con `python3 scripts/update_indexes.py` desde la raíz.\n\n"
                   "[Arquitectura](../../ARCHITECTURE.md) · [Migración](../../MIGRATION.md) · "
                   f"[Agente](../../.claude/agents/finance-app-expert-{agent}.md)\n\n"
                   "## Archivos\n\n```text\npom.xml\n" + "\n".join(files) + "\n```\n")
        (base / name).write_text(content)
    (ROOT / "GENERAL.md").write_text("""# Índice del proyecto

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
""")


if __name__ == "__main__":
    update()
