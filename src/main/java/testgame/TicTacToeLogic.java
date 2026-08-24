package testgame;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import common.JsonPacket;
import server.ClientSession;
import server.ServerListener;

/**
 * Implements the game logic for a Tic-Tac-Toe game, managing matchmaking and game sessions.
 */
public class TicTacToeLogic implements ServerListener {
    private final Queue<ClientSession> waitingQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, GameSession> playerMatches = new ConcurrentHashMap<>();
    private final NeuralNetworkClient neuralNetworkClient;

    public TicTacToeLogic() {
        this("http://127.0.0.1:8001/predict");
    }

    public TicTacToeLogic(String predictionUrl) {
        this.neuralNetworkClient = new NeuralNetworkClient(predictionUrl);
    }

    @Override
    public void onClientConnected(ClientSession client) {
        System.out.println("[GAME_LOGIC] New client connected: " + client.getId());
        client.sendPacket(new JsonPacket("SYSTEM", "Connected to the server. Enter your name and search for a match.", "Server"));
    }

    @Override
    public void onClientDisconnected(ClientSession client) {
        System.out.println("[GAME_LOGIC] Client disconnected: " + client.getId());
        waitingQueue.remove(client);
        GameSession session = playerMatches.remove(client.getId());
        if (session != null) {
            session.handleDisconnect(client);
        }
    }

    @Override
    public void onPacketReceived(ClientSession client, JsonPacket packet) {
        String command = packet.getCommand();

        if ("PLAY".equals(command)) {
            client.setUsername(packet.getSender());
            removeCompletedMatch(client);
            if (!waitingQueue.contains(client)) {
                waitingQueue.add(client);
                client.sendPacket(new JsonPacket("SYSTEM", "Searching for an opponent...", "Server"));
                checkMatchmaking();
            }
        } else if ("PLAY_AI".equals(command)) {
            client.setUsername(packet.getSender());
            removeCompletedMatch(client);
            if (!playerMatches.containsKey(client.getId()) && !waitingQueue.contains(client)) {
                GameSession game = new GameSession(client, neuralNetworkClient);
                playerMatches.put(client.getId(), game);
                new Thread(game, "tic-tac-toe-ai").start();
            }
        } else if ("MOVE".equals(command)) {
            GameSession session = playerMatches.get(client.getId());
            if (session != null) {
                try {
                    int cellIndex = Integer.parseInt(packet.getContent());
                    session.processMove(client, cellIndex);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private void removeCompletedMatch(ClientSession client) {
        GameSession session = playerMatches.get(client.getId());
        if (session != null && !session.isGameActive()) {
            playerMatches.remove(client.getId(), session);
        }
    }

    private synchronized void checkMatchmaking() {
        while (waitingQueue.size() >= 2) {
            ClientSession player1 = waitingQueue.poll();
            ClientSession player2 = waitingQueue.poll();

            if (player1 == null || player2 == null) break;

            GameSession game = new GameSession(player1, player2);
            playerMatches.put(player1.getId(), game);
            playerMatches.put(player2.getId(), game);

            new Thread(game).start();
        }
    }
}
