package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    @FXML
    private Button btn3x3;

    @FXML
    private Button btn4x4;

    @FXML
    private Button btn5x5;

    @FXML
    private Label statusLabel;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private NetworkGameView networkGameView;

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

        // Create a network game view (but don't show it yet)
        networkGameView = new NetworkGameView(size, player, SERVER_HOST, SERVER_PORT);

        // Disable the size buttons
        disableSizeButtons();

        // Update status to show we're waiting
        if (statusLabel != null) {
            statusLabel.setText("Connecting to server and waiting for opponent...");
        }

        // Connect to the server
        if (!networkGameView.connect()) {
            // Connection failed
            enableSizeButtons();
            if (statusLabel != null) {
                statusLabel.setText("Failed to connect to server. Please try again.");
            }
        }

        // Note: We don't close the registration window - player stays here until matched
    }

    /**
     * Disable the size selection buttons
     */
    private void disableSizeButtons() {
        if (btn3x3 != null) btn3x3.setDisable(true);
        if (btn4x4 != null) btn4x4.setDisable(true);
        if (btn5x5 != null) btn5x5.setDisable(true);
    }

    /**
     * Enable the size selection buttons
     */
    private void enableSizeButtons() {
        if (btn3x3 != null) btn3x3.setDisable(false);
        if (btn4x4 != null) btn4x4.setDisable(false);
        if (btn5x5 != null) btn5x5.setDisable(false);
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