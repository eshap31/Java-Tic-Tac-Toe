package com.example.demo;

/**
 * Game class represents a single Tic-Tac-Toe game between two players.
 * This class is part of the Model in the MVC pattern and contains no UI elements.
 */
public class Game {
    private Player player1;
    private Player player2;
    private Board gameBoard;
    private int currentPlayerIndex; // 0 for player1, 1 for player2
    private GameState state;
    private Player winner;
    private Move lastMove;

    /**
     * Constructor for a new game
     * @param player1 First player (typically uses 'X')
     * @param player2 Second player (typically uses 'O')
     * @param boardSize Size of the game board (e.g., 3 for a 3x3 board)
     */
    public Game(Player player1, Player player2, int boardSize) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameBoard = new Board(boardSize);
        this.currentPlayerIndex = 0; // player1 starts
        this.state = GameState.WAITING_TO_START;
    }

    /**
     * Starts the game
     */
    public void start() {
        if (this.state == GameState.WAITING_TO_START) {
            this.gameBoard.initializeBoard();
            this.state = GameState.IN_PROGRESS;
        }
    }

    /**
     * Processes a move from the current player
     * @param move The move to be made
     * @return true if the move was valid and processed, false otherwise
     */
    public boolean makeMove(Move move) {
        // Validate the move
        if (this.state != GameState.IN_PROGRESS) {
            return false;
        }

        // Check if the cell is already taken
        if (!gameBoard.isCellEmpty(move.getX(), move.getY())) {
            return false;
        }

        // Set the current player's symbol for this move
        Player currentPlayer = getCurrentPlayer();
        move.setSymbol(currentPlayer.getSymbol());

        // Make the move on the board
        int result = gameBoard.makeMove(move);
        this.lastMove = move;

        // Process the result
        if (result == 1) {
            // Player won
            this.state = GameState.PLAYER_WON;
            this.winner = currentPlayer;
        } else if (result == 0) {
            // Tie
            this.state = GameState.TIE;
        } else {
            // Game continues
            switchPlayer();
        }

        return true;
    }

    /**
     * Switches the current player
     */
    private void switchPlayer() {
        this.currentPlayerIndex = (this.currentPlayerIndex + 1) % 2;
    }

    /**
     * Gets the current player
     * @return The current player
     */
    public Player getCurrentPlayer() {
        return (currentPlayerIndex == 0) ? player1 : player2;
    }

    /**
     * Gets the current state of the game
     * @return The current game state
     */
    public GameState getState() {
        return state;
    }

    /**
     * Gets the winner of the game, if any
     * @return The winning player, or null if no winner yet
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Gets the last move made in the game
     * @return The last move, or null if no moves made yet
     */
    public Move getLastMove() {
        return lastMove;
    }

    /**
     * Gets the current board state
     * @return The current state of the game board
     */
    public char[][] getBoardState() {
        return gameBoard.getGrid();
    }

    /**
     * Gets the first player
     * @return The first player
     */
    public Player getPlayer1() {
        return player1;
    }

    /**
     * Gets the second player
     * @return The second player
     */
    public Player getPlayer2() {
        return player2;
    }

    /**
     * Gets the game board
     * @return The game board
     */
    public Board getGameBoard() {
        return gameBoard;
    }

    /**
     * Enum representing possible game states
     */
    public enum GameState {
        WAITING_TO_START,
        IN_PROGRESS,
        PLAYER_WON,
        TIE
    }
}