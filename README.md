# Finance app

Aplicación de finanzas personales en Java 17: cliente Swing, servidor mediante sockets
con mensajes JSON y persistencia MySQL.

[Arquitectura](ARCHITECTURE.md) · [Mapa del proyecto](PROJECT_MAP.md) · [Instrucciones](AGENTS.md)


# Configuración de Base de Datos MySQL con Docker

Este proyecto utiliza **MySQL** como motor de base de datos, ejecutado dentro de un contenedor **Docker** para facilitar la instalación.

Pasos necesarios para **preparar y levantar la base de datos localmente**.

---

## Requisitos previos

Antes de comenzar, asegúrate de tener instalado:

- Docker
- Docker CLI funcionando correctamente

Puedes verificar Docker con:

```bash
docker --version
```

1. Descargar la imagen de MySQL

Primero, es necesario descargar la imagen oficial de MySQL desde Docker Hub:

docker pull mysql

Esto descargará la versión más reciente de MySQL disponible.


2. Crear y ejecutar el contenedor de MySQL

Una vez descargada la imagen, se debe crear y ejecutar un contenedor configurando las variables de entorno necesarias.

Ejemplo de ejecución

``` bash
docker run --name <nombre-del-contenedor> \
  -e MYSQL_ROOT_PASSWORD=<password-root> \
  -e MYSQL_DATABASE=<nombre-base-datos> \
  -e MYSQL_USER=<usuario-db> \
  -e MYSQL_PASSWORD=<password-usuario> \
  -p <puerto-local>:3306 \
  -d mysql
```


3. Verificar que el contenedor esté en ejecución

Puedes comprobar que el contenedor está activo con:

``` bash
docker ps
```
Deberías ver el contenedor con el nombre definido anteriormente.


4. Acceder a MySQL dentro del contenedor

Para ingresar a la consola de MySQL directamente desde el contenedor:

``` bash
docker exec -it <nombre-del-contenedor> \
  mysql -u<usuario-db> -p<password-usuario>
```

Una vez dentro, podrás ejecutar comandos SQL normalmente.


6. Detener o eliminar el contenedor

Detener el contenedor

``` bash
docker stop <nombre-del-contenedor>
```
Eliminar el contenedor

``` bash
docker rm <nombre-del-contenedor>
```


## Inicialización de la base de datos

La definición de las tablas no se encuentra directamente en este archivo.

Los scripts SQL se ubican en:

```text
database/schemas.sql
```


## Configuración del archivo `config.properties`

```text
backend/java-server/src/main/resources/config.properties
```

El proyecto utiliza un archivo de configuración `config.properties` para definir los parámetros del **servidor** y de la **base de datos**.

Una vez que la base de datos ha sido creada y ejecutada (por ejemplo, mediante Docker), es necesario **ajustar estos valores para que coincidan con la configuración real del entorno**.

---

### Configuración del Servidor

```properties
server.host=<host-del-servidor>
server.port=<puerto-del-servidor>
```

### Configuración de la Base de Datos

```properties
database.host=<host-base-datos>
database.port=<puerto-base-datos>
database.name=<nombre-base-datos>
database.username=<usuario-base-datos>
database.password=<password-base-datos>
```

---

## 🚀 Cómo generar el JAR

1. Asegúrate de tener correctamente estructurado el proyecto:

```
java-shared/
├─ src/main/java/com/giozar04/
│   ├─ <feature>/...
│   ├─ logging/  messages/  shared/
├─ pom.xml
```

2. Declara en `pom.xml` que es un JAR:

```xml
<packaging>jar</packaging>
```

3. Ejecuta:

```bash
mvn clean install
```

Esto compilará el código y generará un archivo `.jar` dentro de `target/`, e instalará el artefacto en tu repositorio local (`~/.m2/repository`).

---

## 🔗 Cómo usarlo en `java-serve` y `java-client`

### 1. Agrega la dependencia en el `pom.xml` de cada proyecto:

```xml
<dependency>
  <groupId>com.giozar04</groupId>
  <artifactId>java-shared</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. No dupliques clases compartidas en servidor ni cliente.

### 3. Importa desde el paquete real `com.giozar04.<feature>`:

```java
import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
```

Haz esto en **servidor** y **cliente**.

---

## ✅ Compilar y ejecutar

Compila los proyectos en el siguiente orden:

```bash
cd shared/java-shared
mvn clean install
cd ../../
cd backend/java-server
mvn clean install
cd ../../
cd client/java-client
mvn clean install
cd ../../
```

Luego ejecuta el servidor y el cliente. Deberían comunicarse correctamente usando las clases compartidas desde `java-shared`.

---

## 🧠 Nota

Si en un futuro agregas campos nuevos a las clases compartidas, solo debes:

1. Editar `java-shared`.
2. Ejecutar `mvn clean install`.
3. Volver a compilar `java-server` y `java-client` (y adaptarlos si cambió el contrato).

---

## 🛠️ Requisitos

- Java 17
- Apache Maven

---
