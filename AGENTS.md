# Instrucciones del repositorio

Comuníquese en español. Este archivo es el punto de entrada para cualquier agente o
persona que trabaje en el proyecto: indica qué leer, qué reglas respetar, cómo
verificar y qué documentación actualizar.

## El proyecto

Aplicación de finanzas personales en Java 17 y Maven, organizada en cuatro partes:

| Parte | Ruta | Rol |
| --- | --- | --- |
| Shared | `shared/java-shared` | JAR con entidades, enums, excepciones, mappers y el mensaje del protocolo |
| Backend | `backend/java-server` | Servidor de sockets: casos de uso, reglas y persistencia MySQL |
| Client | `client/java-client` | Aplicación Swing: presentación y comunicación con el servidor |
| Database | `database/` | `schemas.sql` (esquema completo) y `migrations/` (cambios sobre datos existentes) |

```text
client ──Message JSON por socket──▶ backend ──JDBC──▶ MySQL (triggers y procedimientos)
   └──────────── ambos dependen del JAR java-shared ────────────┘
```

Cada módulo Java se organiza por feature y por capas (`domain`, `application`,
`infrastructure` y, en el cliente, `presentation`). La regla de dependencias, el
vocabulario y las reglas de compatibilidad están en [ARCHITECTURE.md](ARCHITECTURE.md).

## Antes de modificar

1. Lea [ARCHITECTURE.md](ARCHITECTURE.md) y localice el módulo en [PROJECT_MAP.md](PROJECT_MAP.md).
2. Lea la guía del módulo (`*_GUIDE.md`) y use su mapa (`*_ARCHITECTURE.md`) para
   ubicar los archivos. Abra solo los archivos de la feature implicada y de una
   feature de referencia: `tags` (caso sencillo) o `accounts` (enums, subpaneles y
   vistas de detalle).
3. Lea el agente del módulo en `.claude/agents/`: contiene contratos, mensajes y
   casos particulares que no están en las guías.
4. Antes de cambiar una API compartida, busque sus consumidores, por ejemplo
   `rg -n "TagMapper" backend client`. Un tipo de mensaje se busca igual:
   `rg -n "GET_ALL_TAGS" backend/java-server/src client/java-client/src`.
5. Si un caso de uso, una regla de negocio o la capa donde debe vivir una validación
   no está clara, pregunte antes de implementar.

| Tarea | Módulos que suelen cambiar | Especialistas |
| --- | --- | --- |
| Campo nuevo en una entidad persistida | database → shared → backend → client | database, shared, backend, client |
| Operación o tipo de mensaje nuevo | shared (si cambia el contrato) → backend → client | backend, client |
| Pantalla, formulario o componente visual | client | client |
| Regla de saldos, trigger o procedimiento | database (y backend si cambia el mensaje de error) | database |
| Commit, rama o descripción de PR | — | git |

## Reglas del proyecto

- **Protocolo:** conserve los tipos de mensaje, las claves camelCase de `data`, los
  códigos de enums, nulls, valores por defecto, tipos numéricos, fechas y mensajes
  de error. El cliente y el servidor deben seguir entendiéndose sin cambios ocultos.
- **Saldos:** los efectos sobre saldos los aplican los triggers y procedimientos de
  MySQL. Solo las transacciones `COMPLETED` afectan saldos. El backend valida y
  persiste; no recalcula saldos.
- **Esquema:** `database/schemas.sql` es la fuente de verdad y ejecuta
  `DROP DATABASE`. No lo ejecute para probar código. Todo cambio que deba aplicarse
  sobre datos existentes lleva una migración `database/migrations/AAAA-MM-DD_<nombre>.sql`.
- **Nombres heredados:** no renombre paquetes, entidades, propiedades ni enums. En
  shared los paquetes son `card` y `bankClient`; en backend y client son `cards` y
  `bankClients`.
- **Excepciones:** cada excepción nueva va en su propio archivo con nombre de feature;
  no cree contenedores `*Exceptions` ni utilidades genéricas nuevas.
- **Textos de interfaz y errores:** en español y en tono formal de usted
  ("Seleccione…", "Indique…").
- **Configuración:** `config.properties` es local y está ignorado por Git; las
  plantillas son `config.example.properties` en backend y client. No suba credenciales.
- **Alcance:** haga los cambios de otros módulos que la tarea necesita para funcionar
  de extremo a extremo y explíquelos. Proponga antes cualquier mejora no necesaria.

## Verificación

Compile desde la raíz y en orden de dependencias, porque backend y client usan el JAR
instalado de shared. El comando deja la terminal en la raíz al terminar:

```bash
(cd shared/java-shared && mvn clean install) && (cd backend/java-server && mvn clean install) && (cd client/java-client && mvn clean install)
```

Las pruebas de `src/test` son programas `main` sin JUnit: `mvn test` no las ejecuta.
Después de compilar, ejecútelas cuando el cambio toque contratos o casos de uso:

```bash
# Shared: el protocolo debe coincidir con la línea base src/test/resources/contracts.json
(cd shared/java-shared \
  && java -cp target/classes:target/test-classes com.giozar04.contracts.ContractProbe > /tmp/contracts.out.json \
  && python3 -c 'import json; o=json.load(open("/tmp/contracts.out.json"))["data"]; r=json.load(open("src/test/resources/contracts.json")); d=[k for k in set(o)|set(r) if k not in o or k not in r or json.loads(o[k])!=r[k]]; print("diferencias:", sorted(d))')

# Backend: casos de uso sin JDBC ni sockets (terminan sin salida si pasan)
(cd backend/java-server \
  && CP=target/classes:target/test-classes:$HOME/.m2/repository/com/giozar04/java-shared/1.0-SNAPSHOT/java-shared-1.0-SNAPSHOT.jar \
  && java -cp "$CP" com.giozar04.tags.TagUseCaseProbe \
  && java -cp "$CP" com.giozar04.transactions.TransactionUseCaseProbe)
```

Si cambia intencionalmente un contrato, actualice `contracts.json` en el mismo cambio
y explique la diferencia. Las clases `<feature>/test/*TestApp` y `*FunctionalTest` de
`src/main` son pruebas manuales que requieren MySQL o el servidor en ejecución.

## Documentación que se actualiza con el código

| Si cambia… | Actualice |
| --- | --- |
| Archivos o carpetas de un módulo | `*_ARCHITECTURE.md` del módulo |
| Flujo, convenciones o pasos para implementar | `*_GUIDE.md` del módulo |
| Contratos, mensajes, reglas o casos particulares | agente del módulo en `.claude/agents/` |
| Tablas, triggers, procedimientos o migraciones | índice del agente database |
| Regla de dependencias, vocabulario o compatibilidad | `ARCHITECTURE.md` |
| Módulos, guías o agentes disponibles | `PROJECT_MAP.md` y este archivo |
| Instalación, configuración o compilación | `README.md` |

Antes de terminar, busque en los `.md` los nombres antiguos que haya cambiado.

## Commits y ramas

Haga commits solo cuando la tarea lo autorice, siguiendo al agente
[git](.claude/agents/finance-app-expert-git.md):

- Conventional Commits en español con la descripción en minúscula:
  `docs(client): completar la guía del cliente`.
- Un commit por cambio lógico que compile, con rutas explícitas en `git add`.
- Ramas `<tipo>/<descripcion>` creadas desde `main` o desde la rama de la que dependan.
  La documentación y el contexto de agentes van en su propia rama `docs/...`.
- No incluya credenciales, `target/` ni configuraciones locales.
- No haga push, merge ni abra PRs sin confirmación explícita.

## Especialistas

Los agentes de `.claude/agents/` son guías de contexto por módulo; no son procesos
en ejecución. Cuando una tarea cruza módulos, cada parte sigue la guía de su módulo
y el cambio se coordina con sus consumidores.

| Especialista | Úselo para |
| --- | --- |
| [shared](.claude/agents/finance-app-expert-shared.md) | Entidades, enums, excepciones, mappers y mensaje del protocolo |
| [backend](.claude/agents/finance-app-expert-backend.md) | Casos de uso, políticas, repositorios MySQL, controllers, handlers y bootstrap |
| [client](.claude/agents/finance-app-expert-client.md) | Vistas, formularios, componentes Swing, gateways socket y navegación |
| [database](.claude/agents/finance-app-expert-database.md) | Esquema, migraciones, triggers, procedimientos y reglas de saldos |
| [git](.claude/agents/finance-app-expert-git.md) | Commits, ramas y descripciones de PR |
