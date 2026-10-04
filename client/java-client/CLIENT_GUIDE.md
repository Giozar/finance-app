# Guía de client

[Arquitectura común](../../ARCHITECTURE.md) · [Árbol de client](CLIENT_ARCHITECTURE.md)

## Para qué sirve

`java-client` es la aplicación de escritorio Swing. Presenta información, recoge
las acciones del usuario y se comunica con `java-server` por sockets mediante
mensajes JSON. Las entidades, enums, excepciones y mappers del protocolo vienen
del JAR `java-shared`.

La carpeta `com/giozar04/shared/` contiene componentes visuales reutilizables del
cliente; no es el módulo `shared/java-shared`.

## Cómo está construido

El código vive en `src/main/java/com/giozar04/`. `Main` inicia
`bootstrap/ApplicationInitializer`, que abre la conexión, inicializa los casos
de uso y muestra `AppLayout`. `ClientUseCases` registra los puertos de entrada
que usan las vistas. La conexión y el protocolo están en
`serverConnection/infrastructure/transport/socket/`.

Una feature de cliente tiene esta estructura:

```text
<feature>/
├── application/
│   ├── ports/input/<Feature>Operations.java
│   ├── ports/output/<Feature>Gateway.java
│   └── usecases/<Feature>UseCase.java
├── infrastructure/transport/socket/<Feature>Service.java
└── presentation/
    ├── components/
    └── views/
```

El caso de uso implementa el puerto de entrada y llama al gateway. El servicio
de socket implementa el gateway: crea el `Message`, lo envía mediante
`ServerConnectionService`, espera la respuesta, la valida y convierte sus datos
con el mapper de shared. `ApplicationInitializer` conecta el servicio con el
caso de uso y registra este último en `ClientUseCases`. La presentación conoce
el puerto de entrada y los modelos; no necesita construir mensajes de red.

El recorrido normal es:

```text
Main → ApplicationInitializer → conexión y composición
Vista/Formulario → Operations → UseCase → Gateway
               → servicio socket → Message JSON → java-server
```

## Cómo implementar una feature

1. Revise [el árbol](CLIENT_ARCHITECTURE.md), una feature parecida y el contrato
   correspondiente en shared. Confirme también los tipos de mensaje que atiende
   backend.
2. Defina `*Operations`, `*Gateway` y `*UseCase` para las operaciones que la
   interfaz necesita. Mantenga los casos de uso fuera de Swing y del protocolo.
3. Implemente el servicio socket como gateway. Use los códigos y las claves de
   mensaje que ya acuerdan cliente y servidor; valide respuestas y propague los
   errores de operación existentes.
4. Registre el gateway y el caso de uso en `bootstrap/ApplicationInitializer` y
   publique el puerto de entrada con `ClientUseCases`.
5. Cree o actualice los paneles y vistas bajo `presentation`. Si la feature debe
   aparecer en el menú, incorpórela en `SidebarPanel` y en la navegación de
   `AppLayout`.
6. Actualice `CLIENT_ARCHITECTURE.md` si cambia el árbol y este documento o el
   agente client si cambia el flujo. Coordine cualquier cambio del protocolo con
   backend y shared.

## Convenciones de la interfaz

- Reutilice los componentes de `shared/components/`: formularios, tablas,
  `DialogUtil`, `FormValidatorUtils`, `QuickCreateDialog` y `AppLayout`.
- Los formularios grandes se dividen en paneles especializados. El panel principal
  coordina validación, lectura y escritura del modelo; las secciones no deben
  llamarse entre sí.
- Los combos de enums se construyen con `Enum.values()` y muestran `getLabel()`.
  Los campos visibles y mensajes de validación se redactan en tono formal de
  usted.
- Las features con `userId` muestran primero el selector del usuario propietario.
  Para alta rápida de categorías, etiquetas o entidades externas se reutilizan
  sus formularios con `QuickCreateDialog` y `setOnSaved`.
- Los saldos iniciales de cuentas son de solo lectura. Los saldos actuales se
  capturan al crear la cuenta; al editarla se modifican mediante transacciones o
  conciliación.

## Formulario de transactions

`Transaction` representa un agregado que se envía en un único mensaje. La pantalla
usa `TransactionFormContext` para compartir usuario, operación, cuentas, método y
monto; `TransactionFormDataProvider` carga y cachea catálogos. Cada sección del
formulario observa el contexto y valida o aplica sus datos al agregado. El panel
principal coordina las secciones y guarda el resultado.

Las secciones actuales son operación, participantes, método de pago, detalles de
tarjeta, detalles de wallet, clasificación e información general. Las reglas de
qué métodos se permiten viven en `PaymentMethodPolicy`; la sección de UI muestra
esas opciones y limpia campos que dejan de aplicar al cambiar la selección.
Tarjetas y wallets generan detalles distintos dentro del agregado; categorías y
etiquetas admiten creación rápida. Al editar se carga la transacción completa con
`GET_TRANSACTION`.

Los efectos financieros se validan en backend. La UI conserva la regla de mostrar
solo las opciones pertinentes y enviar el agregado con los campos y tipos que
espera el protocolo.
