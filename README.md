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
database/schema.sql
```