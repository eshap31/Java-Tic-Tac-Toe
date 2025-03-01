package com.example.demo;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client for connecting to the game server
 */
public class GameClient implements Runnable {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;

    private String host;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;
    private boolean running = false;

    private int gameId = -1;
    private char playerSymbol;
    private NetworkGameView view;

    /**
     * Creates a game client with the default host and port
     * @param view The game view to update
     */
    public GameClient(NetworkGameView view) {
        this(DEFAULT_HOST, DEFAULT_PORT, view);
    }

    /**
     * Creates a game client with the specified host and port
     * @param host The server host
     * @param port The server port
     * @param view The game view to update
     */
    public GameClient(String host, int port, NetworkGameView view) {
        this.host = host;
        this.port = port;
        this.view = view;
    }

    /**
     * Connects to the server
     * @return true if successful, false otherwise
     */
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            connected = true;
            running = true;

            // Start the thread for receiving messages
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();

            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            Platform.runLater(() -> view.handleConnectionFailed(e.getMessage()));
            return false;
        }
    }

    /**
     * Registers the player with the server
     * @param playerName The player's name
     * @param boardSize The requested board size
     */
    public void registerPlayer(String playerName, int boardSize) {
        if (!connected) {
            return;
        }

        sendMessage("REGISTER:" + playerName + ":" + boardSize);
    }

    /**
     * Makes a move
     * @param row The row
     * @param col The column
     */
    public void makeMove(int row, int col) {
        if (!connected || gameId == -1) {
            return;
        }

        sendMessage("MOVE:" + gameId + ":" + row + ":" + col);
    }

    /**
     * Sends a message to the server
     * @param message The message to send
     */
    private void sendMessage(String message) {
        if (out != null && connected) {
            out.println(message);
        }
    }

    /**
     * Main loop for receiving server messages
     */
    @Override
    public void run() {
        while (running && connected) {
            try {
                String message = in.readLine();
                if (message == null) {
                    // Server closed the connection
                    disconnect();
                    break;
                }

                processMessage(message);
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error reading from server: " + e.getMessage());
                    disconnect();
                }
                break;
            }
        }
    }

    /**
     * Processes a message from the server
     * @param message The message to process
     */
    private void processMessage(String message) {
        System.out.println("Received from server: " + message);

        String[] parts = message.split(":");
        String command = parts[0];

        switch (command) {
            case "REGISTERED":
                if (parts.length >= 2) {
                    final String playerName = parts[1];
                    Platform.runLater(() -> view.handleRegistered(playerName));
                }
                break;

            case "WAITING":
                if (parts.length >= 2) {
                    final String waitMessage = parts[1];
                    Platform.runLater(() -> view.handleWaiting(waitMessage));
                }
                break;

            case "MATCHED":
                if (parts.length >= 4) {
                    this.gameId = Integer.parseInt(parts[1]);
                    this.playerSymbol = parts[2].charAt(0);
                    final String matchMessage = parts[3];
                    Platform.runLater(() -> view.handleMatched(gameId, playerSymbol, matchMessage));
                }
                break;

            case "MOVE":
                if (parts.length >= 4) {
                    final int row = Integer.parseInt(parts[1]);
                    final int col = Integer.parseInt(parts[2]);
                    final char symbol = parts[3].charAt(0);
                    Platform.runLater(() -> view.handleMoveMade(row, col, symbol));
                }
                break;

            case "YOUR_TURN":
                Platform.runLater(() -> view.handleYourTurn());
                break;

            case "OPPONENT_TURN":
                Platform.runLater(() -> view.handleOpponentTurn());
                break;

            case "GAME_OVER":
                if (parts.length >= 2) {
                    final String result = parts[1];
                    String winnerName = "";
                    if (parts.length >= 3 && result.equals("WIN")) {
                        winnerName = parts[2];
                    }
                    final String finalWinnerName = winnerName;
                    Platform.runLater(() -> view.handleGameOver(result, finalWinnerName));
                }
                break;

            case "OPPONENT_DISCONNECTED":
                Platform.runLater(() -> view.handleOpponentDisconnected());
                break;

            case "ERROR":
                if (parts.length >= 2) {
                    final String errorMessage = parts[1];
                    Platform.runLater(() -> view.handleError(errorMessage));
                }
                break;
        }
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        if (!connected) {
            return;
        }

        // Send a disconnect message to the server
        sendMessage("DISCONNECT");

        // Set flags
        connected = false;
        running = false;

        // Close resources
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }

        // Notify the view
        Platform.runLater(() -> view.handleDisconnected());
    }

    /**
     * Checks if the client is connected
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Gets the game ID
     * @return The game ID
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Gets the player's symbol
     * @return The player's symbol
     */
    public char getPlayerSymbol() {
        return playerSymbol;
    }
}