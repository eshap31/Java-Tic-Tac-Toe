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
    private Manager gameManager;

    @Override
    public void start(Stage stage) throws IOException {
        // Create the game manager that will be used throughout the application
        gameManager = new Manager();

        // Print the working directory to debug
        System.out.println("Working Directory: " + System.getProperty("user.dir"));

        // Try multiple possible paths to find the FXML file
        String[] possiblePaths = {
                "hello-view.fxml",
                "/hello-view.fxml",
                "/com/example/demo/hello-view.fxml",
                "com/example/demo/hello-view.fxml"
        };

        URL fxmlUrl = null;
        for (String path : possiblePaths) {
            fxmlUrl = HelloApplication.class.getResource(path);
            if (fxmlUrl != null) {
                System.out.println("Found FXML at: " + path);
                break;
            }
        }

        // If still null, try direct file access (for development only)
        if (fxmlUrl == null) {
            File file = new File("src/main/resources/com/example/demo/hello-view.fxml");
            if (file.exists()) {
                fxmlUrl = file.toURI().toURL();
                System.out.println("Found FXML via direct file access: " + fxmlUrl);
            } else {
                System.err.println("Error: Cannot find FXML file at: " + file.getAbsolutePath());

                // Final fallback - try to create a basic scene without FXML
                stage.setTitle("Tic Tac Toe");
                stage.setScene(new Scene(new javafx.scene.layout.VBox(), 350, 350));
                stage.show();
                return;
            }
        }

        // Load the FXML
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(fxmlLoader.load(), 350, 350);

        // Get the controller and pass the game manager to it
        HelloController controller = fxmlLoader.getController();
        controller.setManager(gameManager);

        // Set up and show the stage
        stage.setTitle("Tic Tac Toe");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}