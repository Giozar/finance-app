# java-shared

Módulo Java con clases y utilidades compartidas entre los proyectos `java-serve` (servidor) y `java-client` (cliente), como `Message`, `Transaction`, `PaymentMethod`, etc.

---

## 📦 ¿Qué contiene este módulo?

- `Message.java`
- `Transaction.java`
- `PaymentMethod.java`
- `TransactionUtils.java`
- `TransactionExceptions.java`

Estas clases son utilizadas por el servidor y el cliente, y ahora están centralizadas para evitar duplicación de código.

---

## 🚀 Cómo generar el JAR

1. Asegúrate de tener correctamente estructurado el proyecto:

```
java-shared/
├─ src/
│  └─ main/
│     └─ java/
│         └─ com/giozar04/shared/
│             ├─ domain/
│             │   └─ Message.java
│             └─ transactions/
│                 ├─ domain/
│                 │   └─ Transaction.java
│                 ├─ enums/
│                 │   └─ PaymentMethod.java
│                 ├─ exceptions/
│                 │   └─ TransactionExceptions.java
│                 └─ application/
│                     └─ TransactionUtils.java
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

### 2. Elimina las clases duplicadas locales (si existen):

- `Message.java`
- `Transaction.java`
- `PaymentMethod.java`
- `TransactionUtils.java`
- `TransactionExceptions.java`

### 3. Ajusta los `import` en tu código:

Antes:
```java
import com.giozar04.transactions.domain.entities.Transaction;
```

Después:
```java
import com.giozar04.shared.transactions.domain.entities.Transaction;
```

Haz esto en **servidor** y **cliente**.

---

## ✅ Compilar y ejecutar

Compila los proyectos en el siguiente orden:

```bash
cd shared/java-shared
mvn clean install

cd backend/java-server
mvn clean install

cd client/java-client
mvn clean install
```

Luego ejecuta el servidor y el cliente. Deberían comunicarse correctamente usando las clases compartidas desde `java-shared`.

---

## 🧠 Nota

Si en un futuro agregas campos nuevos a las clases compartidas, solo debes:

1. Editar `java-shared`.
2. Ejecutar `mvn clean install`.
3. Volver a compilar `java-server` y `java-client`.

---

## 🛠️ Requisitos

- Java 17
- Apache Maven

---


