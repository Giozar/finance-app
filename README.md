# Finance app

Aplicación de finanzas personales en Java 17: cliente de escritorio Swing, servidor
de sockets con mensajes JSON y persistencia MySQL. Registra usuarios, cuentas,
tarjetas, wallets, categorías, etiquetas, entidades externas y transacciones, y
concilia los saldos de cada cuenta.

[Instrucciones para agentes](AGENTS.md) · [Arquitectura](ARCHITECTURE.md) · [Mapa del proyecto](PROJECT_MAP.md)

## Estructura

| Ruta | Contenido |
| --- | --- |
| `shared/java-shared` | JAR común: entidades, enums, excepciones, mappers y mensaje del protocolo |
| `backend/java-server` | Servidor: casos de uso, reglas, repositorios MySQL y transporte por sockets |
| `client/java-client` | Cliente Swing: vistas, formularios y gateways hacia el servidor |
| `database/schemas.sql` | Esquema completo: tablas, triggers, procedimientos y vista de conciliación |
| `database/migrations/` | Scripts incrementales para bases de datos con información |

Backend y client dependen del JAR de shared, por lo que shared se compila primero.

## Requisitos

- Java 17
- Apache Maven
- MySQL 8.0.19 o superior (local o en Docker)

## 1. Base de datos

### Levantar MySQL con Docker (opcional)

```bash
docker run --name finance-mysql \
  -e MYSQL_ROOT_PASSWORD=<password-root> \
  -e MYSQL_USER=<usuario-db> \
  -e MYSQL_PASSWORD=<password-usuario> \
  -p 3306:3306 \
  -d mysql:8
```

Compruebe que el contenedor está activo con `docker ps`. Para detenerlo o eliminarlo
use `docker stop finance-mysql` y `docker rm finance-mysql`.

### Crear el esquema

`database/schemas.sql` crea la base de datos `finanzas` con todas sus tablas, triggers,
procedimientos y vistas. Ejecútelo con un usuario con privilegios de administración,
porque usa `SET GLOBAL log_bin_trust_function_creators = 1`:

```bash
mysql -h 127.0.0.1 -u root -p < database/schemas.sql
# Con Docker:
docker exec -i finance-mysql mysql -uroot -p<password-root> < database/schemas.sql
```

> **Atención:** el script empieza con `DROP DATABASE IF EXISTS finanzas` y borra todos
> los datos. Úselo solo para crear una base nueva.

Si el usuario de la aplicación no es `root`, concédale acceso a la base:

```sql
GRANT ALL PRIVILEGES ON finanzas.* TO '<usuario-db>'@'%';
```

### Actualizar una base existente

Para conservar los datos, aplique en orden de fecha las migraciones de
`database/migrations/` que aún no se hayan ejecutado. No son idempotentes: cada una
debe ejecutarse una sola vez. Revise los diagnósticos comentados del paso 0 de cada
script antes de aplicarlo.

```bash
mysql -h 127.0.0.1 -u root -p finanzas < database/migrations/<AAAA-MM-DD_nombre>.sql
```

## 2. Configuración

Backend y client leen `src/main/resources/config.properties`. El archivo está ignorado
por Git; créelo a partir de la plantilla de cada módulo:

```bash
cp backend/java-server/src/main/resources/config.example.properties \
   backend/java-server/src/main/resources/config.properties
cp client/java-client/src/main/resources/config.example.properties \
   client/java-client/src/main/resources/config.properties
```

Backend:

```properties
server.host=localhost
server.port=8080

database.host=localhost
database.port=3306
database.name=finanzas
database.username=<usuario-db>
database.password=<password-usuario>
```

Client (debe apuntar al mismo host y puerto que el servidor):

```properties
server.host=localhost
server.port=8080
```

## 3. Compilar

```bash
(cd shared/java-shared && mvn clean install)
(cd backend/java-server && mvn clean compile)
(cd client/java-client && mvn clean compile)
```

`mvn install` en shared publica `com.giozar04:java-shared:1.0-SNAPSHOT` en el
repositorio local de Maven (`~/.m2`), desde donde lo toman backend y client.

Cuando cambie una clase de shared, vuelva a ejecutar `mvn clean install` en shared y
recompile backend y client. Si cambió el contrato (campos, enums o firmas), adapte
también sus consumidores.

## 4. Ejecutar

Inicie primero el servidor y después el cliente, cada uno en su terminal:

```bash
(cd backend/java-server && mvn exec:java -Dexec.mainClass=com.giozar04.Main)
(cd client/java-client && mvn exec:java -Dexec.mainClass=com.giozar04.Main)
```

También puede ejecutar la clase `com.giozar04.Main` de cada módulo desde el IDE.

## 5. Verificar

Las pruebas de `src/test` son programas `main` (sin JUnit) que comprueban el protocolo
y los casos de uso sin base de datos. Los comandos para ejecutarlas están en
[AGENTS.md](AGENTS.md#verificación).

## Documentación

| Documento | Para qué sirve |
| --- | --- |
| [AGENTS.md](AGENTS.md) | Flujo de trabajo, reglas, verificación y especialistas |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Capas, regla de dependencias, vocabulario y compatibilidad |
| [PROJECT_MAP.md](PROJECT_MAP.md) | Guías, mapas de archivos y agentes de cada módulo |
