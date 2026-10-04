# Instrucciones del repositorio

Comuníquese en español. Consulte [ARCHITECTURE.md](ARCHITECTURE.md) y
[MIGRATION.md](MIGRATION.md) antes de modificar una feature.

## Flujo de trabajo

1. Localice la feature en el índice `GENERAL*.md` y lea el agente correspondiente.
2. Identifique los consumidores antes de cambiar una API compartida.
3. Aplique la regla de dependencias y el vocabulario de `ARCHITECTURE.md`.
4. Migre una feature por cambio lógico. Actualice los imports y llamadas de sus
   consumidores cuando sea necesario para mantener la compilación.
5. Verifique los contratos con `python3 scripts/verify_shared.py`; revise el diff.
6. Actualice el estado, los índices, las guías afectadas y el agente en ese mismo cambio.
7. Haga commits por feature cuando la tarea lo autorice, con Conventional Commits en español.
   Añada rutas explícitas. No incluya credenciales, `target/` ni configuraciones locales.

La tarea vigente autoriza la reingeniería y los commits locales. La especialización
por módulo permite coordinar actualizaciones necesarias en sus consumidores; no obliga
a detener un cambio ya autorizado por afectar imports de otro módulo.

## Especialistas

- [Shared](.claude/agents/finance-app-expert-shared.md): modelos, excepciones y serialización compartida.
- [Backend](.claude/agents/finance-app-expert-backend.md): casos de uso, puertos y adaptadores del servidor.
- [Client](.claude/agents/finance-app-expert-client.md): presentación y comunicación del cliente.
- [Database](.claude/agents/finance-app-expert-database.md): esquema, migraciones y reglas de saldos.
- [Git](.claude/agents/finance-app-expert-git.md): commits por cambio lógico.

Las especialidades son guías de contexto; no implican procesos de agentes en ejecución.
