package com.example.demo;

import javafx.application.Platform;

import java.util.concurrent.locks.ReentrantLock;

/**
 * GameController handles the communication between the Game model and the GameView.
 * It processes user input from the view and updates the model accordingly.
 * Each controller runs in its own thread for player-specific operations.
 */
public class GameController implements Runnable {
    private Game game;
    private PlayerGameView player1View;
    private PlayerGameView player2View;
    private boolean running = true;
    private final ReentrantLock gameLock = new ReentrantLock();
    private Move lastMove = null;

    /**
     * Constructor for the game controller
     * @param game The game model to control
     */
    public GameController(Game game) {
        this.game = game;
    }

    /**
     * Set the view for player 1
     * @param player1View The view for player 1
     */
    public void setPlayer1View(PlayerGameView player1View) {
        this.player1View = player1View;
    }

    /**
     * Set the view for player 2
     * @param player2View The view for player 2
     */
    public void setPlayer2View(PlayerGameView player2View) {
        this.player2View = player2View;
    }

    /**
     * Start the game and the controller thread
     */
    public void startGame() {
        game.start();
        // Initial update of views
        updateViews();

        // Start the game controller thread
        Thread gameThread = new Thread(this);
        gameThread.setDaemon(true); // Make it a daemon thread so it doesn't prevent app exit
        gameThread.start();

        System.out.println("Game controller thread started");
    }

    /**
     * The run method for the controller thread
     * Continuously checks for game state changes and updates views
     */
    @Override
    public void run() {
        while (running) {
            // Get current game state (with thread safety)
            Game.GameState currentState;
            gameLock.lock();
            try {
                currentState = game.getState();
            } finally {
                gameLock.unlock();
            }

            // Monitor game state changes
            switch (currentState) {
                case IN_PROGRESS:
                    // Continue running
                    break;
                case PLAYER_WON:
                case TIE:
                    // Game is over - update UI on JavaFX thread
                    Platform.runLater(this::updateViews);
                    running = false;
                    break;
                default:
                    break;
            }

            // Pause between checks to avoid excessive CPU usage
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }

        System.out.println("Game controller thread stopped");
    }

    /**
     * Process a move at the given position
     * @param row Row index
     * @param col Column index
     * @param player The player making the move
     * @return true if the move was valid, false otherwise
     */
    public boolean makeMove(int row, int col, Player player) {
        if (!isPlayerTurn(player)) {
            return false;
        }

        Move move = new Move(row, col, player.getSymbol());

        // Lock for thread safety when modifying the game state
        gameLock.lock();
        boolean moveSuccessful;
        try {
            moveSuccessful = game.makeMove(move);

            if (moveSuccessful) {
                lastMove = move;
                // Schedule UI update on JavaFX thread
                Platform.runLater(this::updateViews);
            }
        } finally {
            gameLock.unlock();
        }

        return moveSuccessful;
    }

    /**
     * Check if it's the given player's turn
     * @param player The player to check
     * @return true if it's the player's turn, false otherwise
     */
    public boolean isPlayerTurn(Player player) {
        gameLock.lock();
        try {
            return game.getCurrentPlayer() == player;
        } finally {
            gameLock.unlock();
        }
    }

    /**
     * Update both player views to reflect the current game state
     * Must be called on the JavaFX Application Thread
     */
    private void updateViews() {
        if (player1View != null && player2View != null) {
            // Get all the state we need while holding the lock
            char[][] boardState;
            Player currentPlayer;
            Player winner;
            Game.GameState gameState;

            gameLock.lock();
            try {
                boardState = game.getBoardState();
                currentPlayer = game.getCurrentPlayer();
                gameState = game.getState();
                winner = game.getWinner();
            } finally {
                gameLock.unlock();
            }

            // Update player 1's view
            // No need for additional Platform.runLater as PlayerGameView methods already use it
            player1View.updateBoard(boardState);
            player1View.updateTurnStatus(currentPlayer == player1View.getPlayer());
            updateGameStatus(player1View, gameState, winner);

            // Update player 2's view
            player2View.updateBoard(boardState);
            player2View.updateTurnStatus(currentPlayer == player2View.getPlayer());
            updateGameStatus(player2View, gameState, winner);
        }
    }

    /**
     * Update game status for a specific player view
     */
    private void updateGameStatus(PlayerGameView playerView, Game.GameState gameState, Player winner) {
        switch (gameState) {
            case WAITING_TO_START:
                playerView.showStatus("Game is about to start");
                break;

            case IN_PROGRESS:
                // Status already updated in updateTurnStatus
                break;

            case PLAYER_WON:
                if (winner == playerView.getPlayer()) {
                    playerView.showGameOver("You won!");
                } else {
                    playerView.showGameOver("You lost!");
                }
                break;

            case TIE:
                playerView.showGameOver("Game over! It's a tie!");
                break;
        }
    }

    /**
     * Handle a player disconnect
     * @param player The player who disconnected
     */
    public void handlePlayerDisconnect(Player player) {
        running = false;

        // Use Platform.runLater to update UI on disconnect
        Platform.runLater(() -> {
            // If one player disconnects, notify the other and close their window
            if (player1View != null && player2View != null) {
                if (player == player1View.getPlayer() && player2View != null) {
                    player2View.showGameOver("Opponent disconnected. Game over.");
                } else if (player == player2View.getPlayer() && player1View != null) {
                    player1View.showGameOver("Opponent disconnected. Game over.");
                }
            }
        });
    }

    /**
     * Stop the controller thread
     */
    public void stop() {
        running = false;
    }

    /**
     * Get the current game
     * @return The game being controlled
     */
    public Game getGame() {
        return game;
    }
}