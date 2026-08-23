package testgame;

import common.JsonPacket;
import server.ClientSession;

public class GameSession implements Runnable {
    private final ClientSession playerX;
    private final ClientSession playerO;
    private final String[] board = new String[9];
    
    private boolean isXTurn = true;
    private boolean gameActive = true;
    
    // Variables to track the number of pieces each player has on the board and the selected piece for moving.
    private int piecesX = 0;
    private int piecesO = 0;
    private Integer selectedPieceIndex = null; // Store the index of the selected piece for moving.

    public GameSession(ClientSession playerX, ClientSession playerO) {
        this.playerX = playerX;
        this.playerO = playerO;
        for (int i = 0; i < 9; i++) {
            board[i] = "";
        }
    }

    @Override
    public void run() {
        playerX.sendPacket(new JsonPacket("GAME_START", "X|" + playerO.getUsername(), "Server"));
        playerO.sendPacket(new JsonPacket("GAME_START", "O|" + playerX.getUsername(), "Server"));

        playerX.sendPacket(new JsonPacket("TURN", "Put your piece (X)", "Server"));
        playerO.sendPacket(new JsonPacket("WAIT", "Turn of " + playerX.getUsername(), "Server"));
    }

    public synchronized void processMove(ClientSession sender, int cellIndex) {
        if (!gameActive || cellIndex < 0 || cellIndex >= 9) return;

        boolean isSenderX = (sender == playerX);
        if (isSenderX != isXTurn) {
            sender.sendPacket(new JsonPacket("ERROR", "Not your turn.", "Server"));
            return;
        }

        String symbol = isXTurn ? "X" : "O";
        int currentPieces = isXTurn ? piecesX : piecesO;

        // FASE 1: Fill the board with pieces.
        if (currentPieces < 3) {
            if (!board[cellIndex].isEmpty()) {
                sender.sendPacket(new JsonPacket("ERROR", "Square occupied.", "Server"));
                return;
            }
            
            board[cellIndex] = symbol;
            if (isXTurn) piecesX++; else piecesO++;
            
            broadcast(new JsonPacket("BOARD_UPDATE", cellIndex + "," + symbol, "Server"));
            finalizeTurn(sender, symbol);
        } 
        // FASE 2: Move pieces on the board.
        else {
            // If the player selects one of their pieces it will be marked for moving.
            if (board[cellIndex].equals(symbol)) {
                if (Integer.valueOf(cellIndex).equals(selectedPieceIndex)) {
                    selectedPieceIndex = null;
                    sender.sendPacket(new JsonPacket("PIECE_DESELECTED", "", "Server"));
                    return;
                }
                selectedPieceIndex = cellIndex;
                sender.sendPacket(new JsonPacket("PIECE_SELECTED", Integer.toString(cellIndex), "Server"));
                return;
            }
            
            // If the player has already selected a piece and clicks on an empty space.
            if (selectedPieceIndex != null && board[cellIndex].isEmpty()) {
                // Move the selected piece to the new cell
                board[selectedPieceIndex] = "";
                board[cellIndex] = symbol;
                
                // Send two updates: one for the piece being removed and one for the piece being placed.
                broadcast(new JsonPacket("BOARD_UPDATE", selectedPieceIndex + ",", "Server")); // Send empty string to indicate removal.
                broadcast(new JsonPacket("BOARD_UPDATE", cellIndex + "," + symbol, "Server"));
                
                selectedPieceIndex = null; // Reset the selected piece index after moving.
                finalizeTurn(sender, symbol);
            } 
            // Error handling for invalid moves or if the player tries to move without selecting a piece first.
            else if (selectedPieceIndex == null && board[cellIndex].isEmpty()) {
                sender.sendPacket(new JsonPacket("ERROR", "You must select a piece first.", "Server"));
            } else {
                sender.sendPacket(new JsonPacket("ERROR", "Invalid move.", "Server"));
            }
        }
    }

    private void finalizeTurn(ClientSession sender, String symbol) {
        if (checkWin(symbol)) {
            gameActive = false;
            sender.sendPacket(new JsonPacket("GAME_OVER", "¡You win the game!", "Server"));
            ClientSession opponent = (sender == playerX) ? playerO : playerX;
            opponent.sendPacket(new JsonPacket("GAME_OVER", "You lost the game.", "Server"));
            return;
        }

        // Switch turns and notify players.
        isXTurn = !isXTurn;
        String phaseText = (isXTurn ? piecesX : piecesO) < 3 ? "Put a piece" : "Move a piece";
        
        if (isXTurn) {
            playerX.sendPacket(new JsonPacket("TURN", "Your turn: " + phaseText, "Server"));
            playerO.sendPacket(new JsonPacket("WAIT", "Waiting for " + playerX.getUsername(), "Server"));
        } else {
            playerO.sendPacket(new JsonPacket("TURN", "Your turn: " + phaseText, "Server"));
            playerX.sendPacket(new JsonPacket("WAIT", "Waiting for " + playerO.getUsername(), "Server"));
        }
    }

    public synchronized void handleDisconnect(ClientSession disconnected) {
        if (!gameActive) return;
        gameActive = false;
        ClientSession winner = (disconnected == playerX) ? playerO : playerX;
        winner.sendPacket(new JsonPacket("GAME_OVER", "The rival has disconnected. You win by abandonment!", "Server"));
    }

    private boolean checkWin(String s) {
        int[][] winLines = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
            {0, 4, 8}, {2, 4, 6}             // Diagonals
        };
        for (int[] line : winLines) {
            if (board[line[0]].equals(s) && board[line[1]].equals(s) && board[line[2]].equals(s)) {
                return true;
            }
        }
        return false;
    }

    private void broadcast(JsonPacket packet) {
        playerX.sendPacket(packet);
        playerO.sendPacket(packet);
    }
}
