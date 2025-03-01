package com.example.demo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server that manages game connections and relays moves between players
 */
public class GameServer {
    private static final int DEFAULT_PORT = 8888;
    private final int port;
    private ServerSocket serverSocket;
    private boolean running;

    // List to keep track of client handler threads
    private List<Thread> clientThreads;
    private List<Thread> gameThreads;

    // Maps connected clients to their players
    private Map<ClientHandler, Player> connectedPlayers;

    // Maps game IDs to game info
    private Map<Integer, GameInfo> activeGames;

    // Game manager for matchmaking
    private Manager gameManager;

    // Next game ID
    private int nextGameId = 1;

    /**
     * Creates a game server with the default port
     */
    public GameServer() {
        this(DEFAULT_PORT);
    }

    /**
     * Creates a game server with the specified port
     * @param port Port to listen on
     */
    public GameServer(int port) {
        this.port = port;
        this.connectedPlayers = new ConcurrentHashMap<>();
        this.activeGames = new ConcurrentHashMap<>();
        this.gameManager = new Manager();
        this.clientThreads = new ArrayList<>();
        this.gameThreads = new ArrayList<>();
    }

    /**
     * Starts the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Game server started on port " + port);

            // Thread for accepting connections
            Thread acceptThread = new Thread(this::acceptConnections);
            acceptThread.start();

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Accepts incoming client connections
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a handler for this client
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);

                // Create and start a thread for this client
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

                // Add to the list of client threads
                synchronized (clientThreads) {
                    clientThreads.add(clientThread);
                }

            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Registers a player and attempts to find a match
     * @param clientHandler The client handler for the player
     * @param playerName The player's name
     * @param boardSize The requested board size
     */
    public void registerPlayer(ClientHandler clientHandler, String playerName, int boardSize) {
        // Create a player object
        Player player = new Player(playerName, boardSize);

        // Associate the client with the player
        connectedPlayers.put(clientHandler, player);

        // Send acknowledgment to the client
        clientHandler.sendMessage("REGISTERED:" + playerName);

        System.out.println("Player registered: " + playerName + " (board size: " + boardSize + ")");

        // Try to match with another player
        findMatch(clientHandler, player);
    }

    /**
     * Tries to find a match for a player
     * @param clientHandler The client handler
     * @param player The player to match
     */
    private void findMatch(ClientHandler clientHandler, Player player) {
        // Check if there's a waiting player with the same board size
        for (Map.Entry<ClientHandler, Player> entry : connectedPlayers.entrySet()) {
            ClientHandler otherHandler = entry.getKey();
            Player otherPlayer = entry.getValue();

            // Skip the current player and players already in games
            if (otherHandler == clientHandler || isPlayerInGame(otherPlayer)) {
                continue;
            }

            // Check if board sizes match
            if (otherPlayer.getBoardSize() == player.getBoardSize()) {
                // Create a game for these players
                createGame(clientHandler, player, otherHandler, otherPlayer);
                return;
            }
        }

        // No match found, notify the player they're waiting
        clientHandler.sendMessage("WAITING:Looking for an opponent...");
        System.out.println("Player " + player.getName() + " is waiting for a match");
    }

    /**
     * Creates a game between two players
     * @param handler1 Handler for player 1
     * @param player1 Player 1
     * @param handler2 Handler for player 2
     * @param player2 Player 2
     */
    private void createGame(ClientHandler handler1, Player player1,
                            ClientHandler handler2, Player player2) {
        // Assign symbols
        player1.setSymbol('X');
        player2.setSymbol('O');

        // Create the game
        Game game = new Game(player1, player2, player1.getBoardSize());

        // Create a game controller
        GameController controller = new GameController(game);

        // Generate a unique game ID
        int gameId = nextGameId++;

        // Store the game info
        GameInfo gameInfo = new GameInfo(gameId, game, controller, handler1, handler2);
        activeGames.put(gameId, gameInfo);

        // Create a runnable for the game
        Runnable gameRunnable = () -> {
            // Notify players about the match
            handler1.sendMessage("MATCHED:" + gameId + ":" + player1.getSymbol() +
                    ":Playing against " + player2.getName());
            handler2.sendMessage("MATCHED:" + gameId + ":" + player2.getSymbol() +
                    ":Playing against " + player1.getName());

            System.out.println("Created game " + gameId + " between " +
                    player1.getName() + " and " + player2.getName());

            // Start the game
            game.start();

            // The first player (X) has the first turn
            notifyPlayerTurn(gameInfo);
        };

        // Create and start a thread for this game
        Thread gameThread = new Thread(gameRunnable);
        gameThread.start();

        // Add to the list of game threads
        synchronized (gameThreads) {
            gameThreads.add(gameThread);
        }
    }

    /**
     * Processes a move from a player
     * @param clientHandler The client handler
     * @param gameId The game ID
     * @param row The row of the move
     * @param col The column of the move
     */
    public void processMove(ClientHandler clientHandler, int gameId, int row, int col) {
        // Get the game info
        GameInfo gameInfo = activeGames.get(gameId);
        if (gameInfo == null) {
            clientHandler.sendMessage("ERROR:Game not found");
            return;
        }

        // Get the player
        Player player = connectedPlayers.get(clientHandler);
        if (player == null) {
            clientHandler.sendMessage("ERROR:Player not found");
            return;
        }

        // Check if it's this player's turn
        Game game = gameInfo.getGame();
        if (game.getCurrentPlayer() != player) {
            clientHandler.sendMessage("ERROR:Not your turn");
            return;
        }

        // Process the move
        Move move = new Move(row, col, player.getSymbol());
        boolean moveSuccessful = game.makeMove(move);

        if (!moveSuccessful) {
            clientHandler.sendMessage("ERROR:Invalid move");
            return;
        }

        // Notify both players about the move
        String moveMsg = "MOVE:" + row + ":" + col + ":" + player.getSymbol();
        gameInfo.getHandler1().sendMessage(moveMsg);
        gameInfo.getHandler2().sendMessage(moveMsg);

        // Check if the game is over
        if (game.getState() == Game.GameState.PLAYER_WON) {
            // Notify players about the win
            String winnerName = game.getWinner().getName();
            gameInfo.getHandler1().sendMessage("GAME_OVER:WIN:" + winnerName);
            gameInfo.getHandler2().sendMessage("GAME_OVER:WIN:" + winnerName);

            // Remove the game
            activeGames.remove(gameId);

        } else if (game.getState() == Game.GameState.TIE) {
            // Notify players about the tie
            gameInfo.getHandler1().sendMessage("GAME_OVER:TIE");
            gameInfo.getHandler2().sendMessage("GAME_OVER:TIE");

            // Remove the game
            activeGames.remove(gameId);

        } else {
            // Notify whose turn it is now
            notifyPlayerTurn(gameInfo);
        }
    }

    /**
     * Notifies players whose turn it is
     * @param gameInfo The game info
     */
    private void notifyPlayerTurn(GameInfo gameInfo) {
        Game game = gameInfo.getGame();
        Player currentPlayer = game.getCurrentPlayer();

        // Get the client handlers
        ClientHandler handler1 = gameInfo.getHandler1();
        ClientHandler handler2 = gameInfo.getHandler2();

        // Get the players
        Player player1 = connectedPlayers.get(handler1);
        Player player2 = connectedPlayers.get(handler2);

        // Send messages based on whose turn it is
        if (currentPlayer == player1) {
            handler1.sendMessage("YOUR_TURN");
            handler2.sendMessage("OPPONENT_TURN");
        } else {
            handler1.sendMessage("OPPONENT_TURN");
            handler2.sendMessage("YOUR_TURN");
        }
    }

    /**
     * Disconnects a client
     * @param clientHandler The client handler
     */
    public void disconnectClient(ClientHandler clientHandler) {
        // Get the player
        Player player = connectedPlayers.remove(clientHandler);
        if (player == null) {
            return;
        }

        System.out.println("Player disconnected: " + player.getName());

        // Find any games the player is in
        for (Map.Entry<Integer, GameInfo> entry : activeGames.entrySet()) {
            GameInfo gameInfo = entry.getValue();
            Game game = gameInfo.getGame();

            if (game.getPlayer1() == player || game.getPlayer2() == player) {
                int gameId = entry.getKey();

                // Notify the other player
                ClientHandler otherHandler = (gameInfo.getHandler1() == clientHandler) ?
                        gameInfo.getHandler2() : gameInfo.getHandler1();

                otherHandler.sendMessage("OPPONENT_DISCONNECTED");

                // Remove the game
                activeGames.remove(gameId);
                System.out.println("Game " + gameId + " ended due to player disconnect");
                break;
            }
        }
    }

    /**
     * Checks if a player is in a game
     * @param player The player to check
     * @return true if the player is in a game, false otherwise
     */
    private boolean isPlayerInGame(Player player) {
        for (GameInfo gameInfo : activeGames.values()) {
            Game game = gameInfo.getGame();
            if (game.getPlayer1() == player || game.getPlayer2() == player) {
                return true;
            }
        }
        return false;
    }

    /**
     * Stops the server
     */
    public void stop() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        // Interrupt all client threads
        synchronized (clientThreads) {
            for (Thread thread : clientThreads) {
                thread.interrupt();
            }
            clientThreads.clear();
        }

        // Interrupt all game threads
        synchronized (gameThreads) {
            for (Thread thread : gameThreads) {
                thread.interrupt();
            }
            gameThreads.clear();
        }

        System.out.println("Game server stopped");
    }

    /**
     * Class to hold game-related information
     */
    private static class GameInfo {
        private final int gameId;
        private final Game game;
        private final GameController controller;
        private final ClientHandler handler1;
        private final ClientHandler handler2;

        public GameInfo(int gameId, Game game, GameController controller,
                        ClientHandler handler1, ClientHandler handler2) {
            this.gameId = gameId;
            this.game = game;
            this.controller = controller;
            this.handler1 = handler1;
            this.handler2 = handler2;
        }

        public int getGameId() { return gameId; }
        public Game getGame() { return game; }
        public GameController getController() { return controller; }
        public ClientHandler getHandler1() { return handler1; }
        public ClientHandler getHandler2() { return handler2; }
    }
}