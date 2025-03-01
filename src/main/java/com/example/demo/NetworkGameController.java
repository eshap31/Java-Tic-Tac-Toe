package com.example.demo;

/**
 * Controller for a networked Tic Tac Toe game.
 * Mediates between the UI and the network client.
 */
public class NetworkGameController {
    private GameClient client;
    private Player player;

    /**
     * Creates a network game controller
     * @param client The game client for network communication
     * @param player The player using this controller
     */
    public NetworkGameController(GameClient client, Player player) {
        this.client = client;
        this.player = player;
    }

    /**
     * Process a move at the given position
     * The move is sent to the server, which will validate it
     * and notify both players if accepted
     *
     * @param row Row index
     * @param col Column index
     * @return true if the move was sent, false otherwise
     */
    public boolean makeMove(int row, int col) {
        // Check if connected to the server
        if (!client.isConnected()) {
            return false;
        }

        // Send the move to the server
        client.makeMove(row, col);

        // Return true to indicate the move was sent
        // The actual result will come from the server
        return true;
    }

    /**
     * Check if the client is connected to the server
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Get the player associated with this controller
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        client.disconnect();
    }
}