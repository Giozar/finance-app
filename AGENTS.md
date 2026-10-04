# Instrucciones del repositorio

Comuníquese en español. Consulte [ARCHITECTURE.md](ARCHITECTURE.md) antes de modificar una feature.

## Flujo de trabajo

1. Localice el módulo en `PROJECT_MAP.md`, revise su mapa de arquitectura y su guía, y lea el agente correspondiente.
2. Identifique los consumidores antes de cambiar una API compartida.
3. Aplique la regla de dependencias y el vocabulario de `ARCHITECTURE.md`.
4. Coordine los cambios de API con sus consumidores para mantener la compilación y el protocolo.
5. Revise los cambios y ejecute las verificaciones Maven relevantes para los módulos afectados.
6. Si cambia la estructura de archivos, actualice el mapa de arquitectura del módulo y la guía o agente afectados.
7. Haga commits por cambio lógico cuando la tarea lo autorice, con Conventional Commits en español.
   Añada rutas explícitas. No incluya credenciales, `target/` ni configuraciones locales.

Los agentes especializados orientan el trabajo por módulo; coordine los cambios entre
módulos cuando una modificación compartida afecte a sus consumidores.

## Especialistas

- [Shared](.claude/agents/finance-app-expert-shared.md): modelos, excepciones y serialización compartida.
- [Backend](.claude/agents/finance-app-expert-backend.md): casos de uso, puertos y adaptadores del servidor.
- [Client](.claude/agents/finance-app-expert-client.md): presentación y comunicación del cliente.
- [Database](.claude/agents/finance-app-expert-database.md): esquema, migraciones y reglas de saldos.
- [Git](.claude/agents/finance-app-expert-git.md): commits por cambio lógico.

Las especialidades son guías de contexto; no implican procesos de agentes en ejecución.
