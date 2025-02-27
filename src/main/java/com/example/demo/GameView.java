package com.example.demo;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * GameView represents the UI for a single Tic-Tac-Toe game.
 * It displays the board and handles user interactions.
 */
public class GameView {
    private GameController controller;
    private int boardSize;

    /**
     * Constructor for the GameView factory
     * @param controller The controller for this game
     * @param boardSize Size of the game board
     */
    public GameView(GameController controller, int boardSize) {
        this.controller = controller;
        this.boardSize = boardSize;
    }

    /**
     * Create separate views for both players in the game
     * @param game The game to create views for
     */
    public void createPlayerViews(Game game) {
        // Create a view for player 1
        PlayerGameView player1View = new PlayerGameView(
                game.getPlayer1().getName() + "'s Game (" + game.getPlayer1().getSymbol() + ")",
                controller,
                boardSize,
                game.getPlayer1()
        );

        // Create a view for player 2
        PlayerGameView player2View = new PlayerGameView(
                game.getPlayer2().getName() + "'s Game (" + game.getPlayer2().getSymbol() + ")",
                controller,
                boardSize,
                game.getPlayer2()
        );

        // Set the views in the controller so it can update them
        controller.setPlayer1View(player1View);
        controller.setPlayer2View(player2View);

        // Show the views
        player1View.show();
        player2View.show();
    }

    /**
     * Inner class representing an individual player's game view
     */
    public static class PlayerGameView {
        private Stage stage;
        private GridPane boardGrid;
        private Button[][] buttons;
        private Label statusLabel;
        private GameController controller;
        private int boardSize;
        private Player player;

        /**
         * Constructor for a player's game view
         * @param title Title for the game window
         * @param controller The controller for this game
         * @param boardSize Size of the game board
         * @param player The player this view is for
         */
        public PlayerGameView(String title, GameController controller, int boardSize, Player player) {
            this.controller = controller;
            this.boardSize = boardSize;
            this.player = player;
            this.buttons = new Button[boardSize][boardSize];

            // Set up the UI
            createUI(title);
        }

        /**
         * Create the game UI
         * @param title Title for the game window
         */
        private void createUI(String title) {
            // Create a new stage
            stage = new Stage();
            stage.setTitle(title);

            // Create the board grid
            boardGrid = new GridPane();
            boardGrid.setHgap(10);
            boardGrid.setVgap(10);

            // Create the status label
            statusLabel = new Label("Game starting...");

            // Create buttons for each cell
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    Button button = new Button();
                    button.setPrefSize(50, 50);
                    button.setFocusTraversable(false);

                    // Store row and col as final variables for lambda capture
                    final int finalRow = row;
                    final int finalCol = col;

                    // Set action for when button is clicked
                    button.setOnAction(event -> {
                        // Check if it's this player's turn
                        if (controller.isPlayerTurn(player)) {
                            controller.makeMove(finalRow, finalCol, player);
                        } else {
                            statusLabel.setText("Not your turn!");
                        }
                    });

                    // Add to grid and button array
                    boardGrid.add(button, col, row);
                    buttons[row][col] = button;
                }
            }

            // Create a VBox to hold the grid and status label
            VBox layout = new VBox(10);
            layout.getChildren().addAll(boardGrid, statusLabel);
            layout.setAlignment(Pos.CENTER);

            // Set the scene
            Scene scene = new Scene(layout, 50 * boardSize + 40, 50 * boardSize + 80);
            stage.setScene(scene);

            // Handle close event to properly clean up the game
            stage.setOnCloseRequest(event -> {
                controller.handlePlayerDisconnect(player);
            });
        }

        /**
         * Update the board display based on the current game state
         * @param boardState Current state of the board
         */
        public void updateBoard(char[][] boardState) {
            // Update must happen on the JavaFX application thread
            Platform.runLater(() -> {
                for (int row = 0; row < boardSize; row++) {
                    for (int col = 0; col < boardSize; col++) {
                        char cell = boardState[row][col];
                        if (cell != '-') {
                            buttons[row][col].setText(String.valueOf(cell));
                            // Disable all buttons if it's not this player's turn
                            if (!controller.isPlayerTurn(player)) {
                                buttons[row][col].setDisable(true);
                            } else {
                                // Only disable buttons that already have a value
                                buttons[row][col].setDisable(true);
                            }
                        } else {
                            buttons[row][col].setText("");
                            // Only enable empty buttons if it's this player's turn
                            buttons[row][col].setDisable(!controller.isPlayerTurn(player));
                        }
                    }
                }
            });
        }

        /**
         * Display a status message
         * @param message The status message to display
         */
        public void showStatus(String message) {
            Platform.runLater(() -> statusLabel.setText(message));
        }

        /**
         * Enable or disable user input based on whose turn it is
         * @param isThisPlayerTurn Whether it's this player's turn
         */
        public void updateTurnStatus(boolean isThisPlayerTurn) {
            Platform.runLater(() -> {
                for (int row = 0; row < boardSize; row++) {
                    for (int col = 0; col < boardSize; col++) {
                        // Only enable empty buttons
                        if (buttons[row][col].getText().isEmpty()) {
                            buttons[row][col].setDisable(!isThisPlayerTurn);
                        }
                    }
                }

                if (isThisPlayerTurn) {
                    statusLabel.setText("Your turn!");
                } else {
                    statusLabel.setText("Opponent's turn...");
                }
            });
        }

        /**
         * Show game over state
         * @param message The game over message
         */
        public void showGameOver(String message) {
            Platform.runLater(() -> {
                // Disable all buttons
                for (int row = 0; row < boardSize; row++) {
                    for (int col = 0; col < boardSize; col++) {
                        buttons[row][col].setDisable(true);
                    }
                }
                statusLabel.setText(message);
            });
        }

        /**
         * Show the game window
         */
        public void show() {
            stage.show();
        }

        /**
         * Close the game window
         */
        public void close() {
            stage.close();
        }

        /**
         * Get the player associated with this view
         */
        public Player getPlayer() {
            return player;
        }
    }
}