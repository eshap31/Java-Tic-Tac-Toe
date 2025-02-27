package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Controller for the initial screens (welcome and player registration)
 */
public class HelloController {
    @FXML
    private TextField nameEntry;

    private Manager manager;
    private LobbyController lobbyController;

    /**
     * Set the game manager
     * @param manager The game manager
     */
    public void setManager(Manager manager) {
        this.manager = manager;
        this.lobbyController = new LobbyController(manager);
    }

    /**
     * Handler for the client creation button
     * @param actionEvent The action event
     */
    @FXML
    public void clientCreated(ActionEvent actionEvent) {
        System.out.println("Client clicked");
        try {
            // Try to find the FXML file
            URL fxmlUrl = null;

            // Try relative path first
            fxmlUrl = HelloController.class.getResource("client_login.fxml");

            // If not found, try direct file access as fallback
            if (fxmlUrl == null) {
                File file = new File("src/main/resources/com/example/demo/client_login.fxml");
                if (file.exists()) {
                    fxmlUrl = file.toURI().toURL();
                    System.out.println("Found client_login.fxml via direct file access: " + fxmlUrl);
                } else {
                    System.err.println("Error: Cannot find client_login.fxml at: " + file.getAbsolutePath());
                    return;
                }
            }

            // Load the FXML
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            // Get the controller and set the manager
            HelloController controller = fxmlLoader.getController();
            controller.setManager(this.manager);

            // Create and show a new stage
            Stage clientLoginStage = new Stage();
            clientLoginStage.setTitle("Player Registration");
            Scene scene = new Scene(root);
            clientLoginStage.setScene(scene);
            clientLoginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the board size selection
     * @param size The selected board size
     */
    private void sizeChosen(int size) {
        // Get the player name from the text field, defaulting if empty
        String name = nameEntry.getText();
        if (name == null || name.trim().isEmpty() || name.equals("Enter Name")) {
            name = "Player" + ((int) (Math.random() * 1000));
        }

        System.out.println(name + " chose to play " + size + "x" + size);

        // Create a new player
        Player player = new Player(name, size);

        // Add the player to the manager through the lobby controller
        Game game = lobbyController.registerPlayer(player);

        // If a game was created (player was matched), set up the game view
        if (game != null) {
            // Create a game controller
            GameController gameController = new GameController(game);

            // Create a game view
            String title = player.getName() + "'s Game (" + player.getSymbol() + ")";
            GameView gameView = new GameView(title, gameController, size);

            // Start the game and show the view
            gameController.startGame();
            gameView.show();

            // Close the registration window
            Stage stage = (Stage) nameEntry.getScene().getWindow();
            stage.close();
        } else {
            // No match yet, show waiting message
            Stage stage = (Stage) nameEntry.getScene().getWindow();
            stage.setTitle("Waiting for opponent...");

            // Add an observer to be notified when a game is created
            manager.addObserver(new Manager.ManagerObserver() {
                @Override
                public void onPlayerWaiting(Player waitingPlayer) {
                    // Not needed
                }

                @Override
                public void onGameCreated(Game createdGame) {
                    if (createdGame.getPlayer1() == player || createdGame.getPlayer2() == player) {
                        // This is our game
                        GameController gameController = new GameController(createdGame);
                        String title = player.getName() + "'s Game (" + player.getSymbol() + ")";
                        GameView gameView = new GameView(title, gameController, size);
                        gameController.startGame();
                        gameView.show();

                        // Remove this observer
                        manager.removeObserver(this);

                        // Close the registration window
                        javafx.application.Platform.runLater(() -> stage.close());
                    }
                }

                @Override
                public void onGameEnded(Game endedGame) {
                    // Not needed
                }
            });
        }
    }

    /**
     * Handler for 3x3 board selection
     * @param actionEvent The action event
     */
    @FXML
    public void chose3x3(ActionEvent actionEvent) {
        sizeChosen(3);
    }

    /**
     * Handler for 4x4 board selection
     * @param actionEvent The action event
     */
    @FXML
    public void chose4x4(ActionEvent actionEvent) {
        sizeChosen(4);
    }

    /**
     * Handler for 5x5 board selection
     * @param actionEvent The action event
     */
    @FXML
    public void chose5x5(ActionEvent actionEvent) {
        sizeChosen(5);
    }
}