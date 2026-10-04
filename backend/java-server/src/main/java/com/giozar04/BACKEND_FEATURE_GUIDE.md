# Crear una feature en backend

[Arquitectura](../../../../../../../ARCHITECTURE.md) ·
[Mapa de backend](../../../../../../../backend/java-server/BACKEND_ARCHITECTURE.md)

El servidor recibe mensajes JSON por sockets, ejecuta casos de uso y persiste en
MySQL. Los modelos y los mappers del protocolo vienen de `java-shared`. `tags` es
la referencia simple; todas las features del backend ya siguen esta estructura.

```text
<feature>/
├── domain/policies/<Feature>Policy.java             # si hay reglas puras
├── application/ports/input/<Feature>Operations.java    # operaciones visibles al adaptador de entrada
├── application/ports/output/<Feature>Repository.java   # persistencia requerida por el caso de uso
├── application/usecases/<Feature>UseCase.java
└── infrastructure/
    ├── persistence/mysql/
    │   ├── Abstract<Feature>JdbcRepository.java     # solo si hay código común real
    │   └── <Feature>RepositoryMySQL.java
    └── transport/socket/
        ├── <Feature>Controllers.java
        └── <Feature>Handlers.java
```

`ApplicationInitializer` crea el adaptador MySQL, lo inyecta en el caso de uso y
registra los handlers. El controller traduce `Message` a modelos y vuelve a
serializar con `<Entity>Mapper.toMap/fromMap`. Solo el adaptador de persistencia
conoce conexiones, SQL, commits y rollback. Las políticas del dominio no importan
JDBC, sockets, JSON ni el logger de consola.

En `tags`, `TagUseCase` recibe `TagRepository` y aplica `TagPolicy` antes de escribir;
`TagRepositoryMySQL` también usa esa política para las llamadas directas. Los handlers
consumen `TagOperations`. Los métodos y tipos de mensaje conservan sus nombres para
mantener el protocolo. Las reglas y validaciones conservan sus mensajes y valores
por defecto.

La conexión JDBC y el servidor de sockets viven en `databases/infrastructure/persistence/mysql` y `servers/infrastructure/transport/socket`, respectivamente. Las transacciones se guardan como agregado mediante `TransactionalExecutor` y los
repositorios participantes con una conexión común. Los triggers de MySQL siguen
aplicando los efectos de saldos; respete el orden de escritura de detalles y etiquetas.
Consulte el agente backend para las reglas de transacciones y conciliación.

Al modificar una feature, mantenga actualizado el árbol en `BACKEND_ARCHITECTURE.md`
y el contexto de este agente. Ejecute las pruebas y compilación Maven relevantes.
