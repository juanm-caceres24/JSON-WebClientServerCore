package server;

import io.javalin.websocket.WsContext;
import common.JsonPacket;
import common.ProtocolParser;

/**
 * Represents the WebSocket session of a connected client in the server.
 */
public class ClientSession {
    private final WsContext ctx;
    private String username;

    public ClientSession(WsContext ctx) {
        this.ctx = ctx;
    }

    public String getId() {
        return ctx.sessionId();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sends a JSON packet to the web browser of this client.
     */
    public void sendPacket(JsonPacket packet) {
        if (ctx.session.isOpen()) {
            ctx.send(ProtocolParser.serialize(packet));
        }
    }

    public void close() {
        if (ctx.session.isOpen()) {
            ctx.session.close();
        }
    }
}
