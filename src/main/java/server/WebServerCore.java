package server;

import io.javalin.Javalin;
import common.JsonPacket;
import common.ProtocolParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core of the web server. Serves static web content and manages the lifecycle of WebSockets.
 */
public class WebServerCore {
    private final int port;
    private final ServerListener listener;
    private final Map<String, ClientSession> activeSessions = new ConcurrentHashMap<>();
    private Javalin app;

    public WebServerCore(int port, ServerListener listener) {
        this.port = port;
        this.listener = listener;
    }

    public void start() {
        app = Javalin.create(config -> {
            config.staticFiles.add("/public", io.javalin.http.staticfiles.Location.CLASSPATH);
        }).start(port);

        // WebSocket endpoint for real-time communication with clients.
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ClientSession session = new ClientSession(ctx);
                activeSessions.put(session.getId(), session);
                listener.onClientConnected(session);
            });

            ws.onMessage(ctx -> {
                ClientSession session = activeSessions.get(ctx.sessionId());
                if (session != null) {
                    JsonPacket packet = ProtocolParser.deserialize(ctx.message());
                    listener.onPacketReceived(session, packet);
                }
            });

            ws.onClose(ctx -> {
                ClientSession session = activeSessions.remove(ctx.sessionId());
                if (session != null) {
                    listener.onClientDisconnected(session);
                }
            });

            ws.onError(ctx -> {
                ClientSession session = activeSessions.remove(ctx.sessionId());
                if (session != null) {
                    listener.onClientDisconnected(session);
                }
            });
        });

        System.out.println("[SERVER_CORE] Web server and WebSocket active on port: " + port);
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }
}
