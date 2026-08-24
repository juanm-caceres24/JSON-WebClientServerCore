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

## Partida contra la red neuronal

Inicia el proyecto `NeuralNetwork` en modo API y selecciona la opcion 7 de su
menu. El API escucha en `http://localhost:8081` por defecto. El servidor del
juego usa esa URL para enviar `POST /predict` con un arreglo `inputs` de nueve
valores: `1.0` para una ficha de la maquina, `-1.0` para una ficha rival y
`0.0` para una casilla vacia.

Si la red neuronal esta en otra URL, configura `TICTACTOE_AI_URL` o inicia el
juego con `-Dtictactoe.ai.url=http://host:puerto/predict`. El puerto del API
neuronal tambien puede cambiarse con `NEURAL_NETWORK_PORT` o
`-Dneural.port=puerto`.
