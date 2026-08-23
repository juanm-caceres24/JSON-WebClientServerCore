# JSON-WebClientServerCore

Servidor web y WebSocket en Java usando Javalin y Gson.

## Requisitos

- Java 17 o superior
- Maven 3.8 o superior

## Estructura de paquetes

La ruta de cada archivo debe coincidir con su declaración `package`:

```text
src/main/java/common/JsonPacket.java       -> package common;
src/main/java/common/ProtocolParser.java  -> package common;
src/main/java/server/*.java                -> package server;
src/main/java/testgame/*.java              -> package testgame;
```

Los imports entre paquetes usan, por ejemplo, `import common.JsonPacket;`.

## Dependencias

Las dependencias se agregan dentro de `<dependencies>` en `pom.xml`. Este proyecto
ya incluye:

- Gson: serializacion y deserializacion JSON.
- Javalin: servidor HTTP y WebSocket.
- `slf4j-simple`: implementacion de logs usada por Javalin.

Maven descarga esas dependencias automaticamente al compilar.

## Compilar y ejecutar

Desde la carpeta raiz del proyecto:

```bash
mvn clean compile
mvn package
mvn exec:java
```

El ultimo comando inicia `testgame.MainApp`, solicita el puerto (8080 por
defecto) y sirve la aplicacion en `http://127.0.0.1:8080`.
