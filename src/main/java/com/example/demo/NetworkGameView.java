package com.example.demo;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Game view for networked play
 */
public class NetworkGameView {
    private Stage stage;
    private GridPane boardGrid;
    private Button[][] buttons;
    private Label statusLabel;
    private int boardSize;
    private Player player;
    private GameClient client;
    private NetworkGameController controller;
    private boolean myTurn = false;

    /**
     * Constructor for a networked game view
     * @param title Title for the game window
     * @param boardSize Size of the game board
     * @param player The player this view is for
     * @param host The server host
     * @param port The server port
     */
    public NetworkGameView(String title, int boardSize, Player player, String host, int port) {
        this.boardSize = boardSize;
        this.player = player;
        this.buttons = new Button[boardSize][boardSize];

        // Create the game client
        this.client = new GameClient(host, port, this);

        // Create the controller
        this.controller = new NetworkGameController(client, player);

        // Create the UI
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
        statusLabel = new Label("Connecting to server...");

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
                    if (myTurn && button.getText().isEmpty()) {
                        // Use the controller to make the move
                        if (controller.makeMove(finalRow, finalCol)) {
                            // Disable the button until we get confirmation
                            button.setDisable(true);
                        }
                    }
                });

                // Add to grid and button array
                boardGrid.add(button, col, row);
                buttons[row][col] = button;

                // Disable all buttons initially
                button.setDisable(true);
            }
        }

        // Create a VBox to hold the grid and status label
        VBox layout = new VBox(10);
        layout.getChildren().addAll(boardGrid, statusLabel);
        layout.setAlignment(Pos.CENTER);

        // Set the scene
        Scene scene = new Scene(layout, 50 * boardSize + 40, 50 * boardSize + 80);
        stage.setScene(scene);

        // Handle close event
        stage.setOnCloseRequest(event -> {
            controller.disconnect();
        });
    }

    /**
     * Connect to the server and register the player
     */
    public void connect() {
        if (client.connect()) {
            statusLabel.setText("Connected. Registering player...");
            client.registerPlayer(player.getName(), boardSize);
        } else {
            statusLabel.setText("Failed to connect to server");
        }
    }

    /**
     * Updates the board display
     * @param row Row of the move
     * @param col Column of the move
     * @param symbol Symbol to display
     */
    private void updateBoard(int row, int col, char symbol) {
        if (row >= 0 && row < boardSize && col >= 0 && col < boardSize) {
            buttons[row][col].setText(String.valueOf(symbol));
            buttons[row][col].setDisable(true);
        }
    }

    /**
     * Enables or disables all empty buttons
     * @param enable true to enable, false to disable
     */
    private void setButtonsEnabled(boolean enable) {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (buttons[row][col].getText().isEmpty()) {
                    buttons[row][col].setDisable(!enable);
                }
            }
        }
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
        controller.disconnect();
        stage.close();
    }

    //-------------------------------------------------------------------------
    // Methods for handling server messages
    //-------------------------------------------------------------------------

    /**
     * Handle connection failure
     * @param message Error message
     */
    public void handleConnectionFailed(String message) {
        statusLabel.setText("Connection failed: " + message);
        showAlert("Connection Error", "Failed to connect to the server: " + message);
    }

    /**
     * Handle player registration
     * @param playerName Player name
     */
    public void handleRegistered(String playerName) {
        statusLabel.setText("Registered as " + playerName);
    }

    /**
     * Handle waiting for opponent
     * @param message Waiting message
     */
    public void handleWaiting(String message) {
        statusLabel.setText(message);
    }

    /**
     * Handle player match
     * @param gameId Game ID
     * @param symbol Player symbol
     * @param message Match message
     */
    public void handleMatched(int gameId, char symbol, String message) {
        statusLabel.setText(message);
        stage.setTitle(player.getName() + "'s Game (" + symbol + ")");
    }

    /**
     * Handle move made
     * @param row Row of the move
     * @param col Column of the move
     * @param symbol Symbol of the move
     */
    public void handleMoveMade(int row, int col, char symbol) {
        updateBoard(row, col, symbol);
    }

    /**
     * Handle your turn notification
     */
    public void handleYourTurn() {
        myTurn = true;
        statusLabel.setText("Your turn!");
        setButtonsEnabled(true);
    }

    /**
     * Handle opponent turn notification
     */
    public void handleOpponentTurn() {
        myTurn = false;
        statusLabel.setText("Opponent's turn...");
        setButtonsEnabled(false);
    }

    /**
     * Handle game over notification
     * @param result Game result (WIN or TIE)
     * @param winnerName Name of the winner (if any)
     */
    public void handleGameOver(String result, String winnerName) {
        myTurn = false;
        setButtonsEnabled(false);

        if (result.equals("WIN")) {
            if (winnerName.equals(player.getName())) {
                statusLabel.setText("Game over! You won!");
            } else {
                statusLabel.setText("Game over! " + winnerName + " won!");
            }
        } else if (result.equals("TIE")) {
            statusLabel.setText("Game over! It's a tie!");
        }
    }

    /**
     * Handle opponent disconnection
     */
    public void handleOpponentDisconnected() {
        myTurn = false;
        setButtonsEnabled(false);
        statusLabel.setText("Your opponent disconnected. Game over.");
        showAlert("Opponent Disconnected", "Your opponent has disconnected from the game.");
    }

    /**
     * Handle disconnection from server
     */
    public void handleDisconnected() {
        myTurn = false;
        setButtonsEnabled(false);
        statusLabel.setText("Disconnected from server");
    }

    /**
     * Handle error notification
     * @param message Error message
     */
    public void handleError(String message) {
        statusLabel.setText("Error: " + message);
    }

    /**
     * Shows an alert dialog
     * @param title The alert title
     * @param message The alert message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
