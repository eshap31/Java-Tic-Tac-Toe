package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Main application class for the Tic Tac Toe game.
 * This class loads the initial FXML and starts the application.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Try to find the FXML file
        URL fxmlUrl = null;

        // Try relative path first
        fxmlUrl = HelloApplication.class.getResource("hello-view.fxml");

        // If not found, try direct file access as fallback
        if (fxmlUrl == null) {
            File file = new File("src/main/resources/com/example/demo/hello-view.fxml");
            if (file.exists()) {
                fxmlUrl = file.toURI().toURL();
                System.out.println("Found hello-view.fxml via direct file access: " + fxmlUrl);
            } else {
                System.err.println("Error: Cannot find hello-view.fxml at: " + file.getAbsolutePath());
                return;
            }
        }

        // Load the FXML
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(fxmlLoader.load(), 350, 350);

        // Get the controller (no need to set Manager anymore)
        HelloController controller = fxmlLoader.getController();

        // Set up and show the stage
        stage.setTitle("Tic Tac Toe");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}