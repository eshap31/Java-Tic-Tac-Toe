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
    private Stage stage;
    private GridPane boardGrid;
    private Button[][] buttons;
    private Label statusLabel;
    private GameController controller;
    private int boardSize;

    /**
     * Constructor for the game view
     * @param title Title for the game window
     * @param controller The controller for this view
     * @param boardSize Size of the game board
     */
    public GameView(String title, GameController controller, int boardSize) {
        this.controller = controller;
        this.boardSize = boardSize;
        this.buttons = new Button[boardSize][boardSize];

        // Set up the view as the controller's view
        controller.setView(this);

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
                    controller.makeMove(finalRow, finalCol);
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
                        buttons[row][col].setDisable(true);
                    } else {
                        buttons[row][col].setText("");
                        buttons[row][col].setDisable(false);
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
     * Disable user input (used when game is over)
     */
    public void disableInput() {
        Platform.runLater(() -> {
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    buttons[row][col].setDisable(true);
                }
            }
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
}