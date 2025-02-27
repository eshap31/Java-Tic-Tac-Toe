package com.example.demo;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsible for setting up game views when a game is created.
 * Acts as an observer of the Manager to create UI when games are matched.
 */
public class GameSetupController implements Manager.ManagerObserver {
    private Manager manager;
    private Map<Player, Stage> waitingStages;

    /**
     * Constructor for the game setup controller
     * @param manager The game manager to observe
     */
    public GameSetupController(Manager manager) {
        this.manager = manager;
        this.waitingStages = new HashMap<>();
        // Register as an observer
        manager.addObserver(this);
    }

    @Override
    public void onPlayerWaiting(Player player) {
        // Store the registration window stage reference if available
        Platform.runLater(() -> {
            for (Window window : Stage.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    if (stage.getTitle().equals("Waiting for opponent...")) {
                        waitingStages.put(player, stage);
                        System.out.println("Stored waiting stage for: " + player.getName());
                    }
                }
            }
        });
    }

    @Override
    public void onGameCreated(Game game) {
        // This is where we set up game views for both players
        System.out.println("Game created between " + game.getPlayer1().getName() +
                " and " + game.getPlayer2().getName() + " - setting up views");
        setupGameViews(game);

        // Close any waiting stage windows
        Platform.runLater(() -> {
            closeWaitingStage(game.getPlayer1());
            closeWaitingStage(game.getPlayer2());
        });
    }

    @Override
    public void onGameEnded(Game game) {
        // Could handle cleanup here if needed
        System.out.println("Game between " + game.getPlayer1().getName() +
                " and " + game.getPlayer2().getName() + " has ended");
    }

    /**
     * Set up game views for a newly created game
     * @param game The game to create views for
     */
    private void setupGameViews(Game game) {
        // Create a controller for the game
        GameController gameController = new GameController(game);

        // Create a game view factory
        GameView gameView = new GameView(gameController, game.getPlayer1().getBoardSize());

        // Create separate views for both players
        gameView.createPlayerViews(game);

        // Start the game
        gameController.startGame();

        System.out.println("Game views created for " + game.getPlayer1().getName() +
                " and " + game.getPlayer2().getName());
    }

    /**
     * Close the waiting stage for a player
     * @param player The player whose waiting stage should be closed
     */
    private void closeWaitingStage(Player player) {
        Stage stage = waitingStages.remove(player);
        if (stage != null) {
            stage.close();
            System.out.println("Closed waiting stage for: " + player.getName());
        }
    }
}