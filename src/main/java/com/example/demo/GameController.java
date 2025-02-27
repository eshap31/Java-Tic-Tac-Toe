package com.example.demo;

/**
 * GameController handles the communication between the Game model and the GameView.
 * It processes user input from the view and updates the model accordingly.
 */
public class GameController {
    private Game game;
    private GameView view;

    /**
     * Constructor for the game controller
     * @param game The game model to control
     */
    public GameController(Game game) {
        this.game = game;
    }

    /**
     * Set the view associated with this controller
     * @param view The view to associate
     */
    public void setView(GameView view) {
        this.view = view;
    }

    /**
     * Start the game
     */
    public void startGame() {
        game.start();
        updateView();
    }

    /**
     * Process a move at the given position
     * @param row Row index
     * @param col Column index
     * @return true if the move was valid, false otherwise
     */
    public boolean makeMove(int row, int col) {
        Player currentPlayer = game.getCurrentPlayer();
        Move move = new Move(row, col, currentPlayer.getSymbol());

        boolean moveSuccessful = game.makeMove(move);

        if (moveSuccessful) {
            updateView();
        }

        return moveSuccessful;
    }

    /**
     * Update the view to reflect the current game state
     */
    private void updateView() {
        if (view != null) {
            // Update the board display
            view.updateBoard(game.getBoardState());

            // Update game status
            switch (game.getState()) {
                case WAITING_TO_START:
                    view.showStatus("Game is about to start");
                    break;
                case IN_PROGRESS:
                    Player currentPlayer = game.getCurrentPlayer();
                    view.showStatus(currentPlayer.getName() + "'s turn (" + currentPlayer.getSymbol() + ")");
                    break;
                case PLAYER_WON:
                    Player winner = game.getWinner();
                    view.showStatus("Game over! " + winner.getName() + " wins!");
                    view.disableInput();
                    break;
                case TIE:
                    view.showStatus("Game over! It's a tie!");
                    view.disableInput();
                    break;
            }
        }
    }

    /**
     * Get the current game
     * @return The game being controlled
     */
    public Game getGame() {
        return game;
    }
}