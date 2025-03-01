package com.example.demo;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*
 * Factory class for creating player-specific game views
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
}