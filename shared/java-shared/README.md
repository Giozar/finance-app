# java-shared

Módulo Java con los contratos compartidos entre `backend/java-server` (servidor) y `client/java-client` (cliente): entidades, enums, excepciones y utilidades de conversión. No contiene lógica de negocio ni UI. Sin dependencias externas (JSON propio en `JsonUtils`).

El árbol completo de archivos está en `GENERALSHARED.md` y la guía para crear una feature en `src/main/java/com/giozar04/shared-explanation.md`.

---

## 📦 ¿Qué contiene este módulo?

Paquete raíz: `com.giozar04.<feature>` (no existe el prefijo `com.giozar04.shared.<feature>`).

Features:
- `users`, `accounts`, `accountCashbackSettings`, `accountReconciliations`, `bankClient`, `card`, `cardTransactionDetails`
- `walletCardLinks`, `walletTransactionDetails`, `categories`, `tags`, `externalEntities`
- `transactions`: raíz de agregado (`Transaction` con `tagIds` y `cardDetail`/`walletDetail` anidados; `TransactionUtils.transactionToMap/mapToTransaction`)

Transversales:
- `messages/domain/models/Message.java` – mensaje cliente ↔ servidor.
- `json/utils/JsonUtils.java` – serialización JSON (escapa/desescapa strings; el literal `null` se lee como `null`).
- `shared/utils/SharedUtils.java` – parseo seguro y formato de fechas.
- `logging/CustomLogger.java` – logger del proyecto.

Cada feature sigue la estructura:

```
<feature>/
├─ application/utils/<Feature>Utils.java
└─ domain/
   ├─ entities/<Feature>.java
   ├─ enums/<Feature>Types.java        (opcional)
   └─ exceptions/<Feature>Exceptions.java
```

Enums: cada constante tiene `(value, label)`; `value` se guarda en MAYÚSCULAS y es igual al nombre de la constante (ej. `INCOME("INCOME", "Ingreso")`), debe coincidir con los CHECK de la base de datos; `label` es el texto en español para UI; `fromValue` no distingue mayúsculas/minúsculas.

Estas clases son utilizadas por el servidor y el cliente, y están centralizadas para evitar duplicación de código.

---

## 🚀 Cómo generar el JAR

1. Asegúrate de tener correctamente estructurado el proyecto:

```
java-shared/
├─ src/main/java/com/giozar04/
│   ├─ <feature>/...
│   ├─ json/  logging/  messages/  shared/
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


