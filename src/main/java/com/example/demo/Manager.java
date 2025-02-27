package com.example.demo;

import java.util.*;

/**
 * Manager class handles player matching and game creation.
 * This class is part of the Model in the MVC pattern and contains no UI elements.
 */
public class Manager {
    private List<Game> activeGames;
    private Map<Integer, List<Player>> waitingPlayers;

    // For observing state changes
    private List<ManagerObserver> observers;

    /**
     * Constructor for the game manager
     */
    public Manager() {
        this.activeGames = new ArrayList<>();
        this.waitingPlayers = new HashMap<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Add a player to the waiting queue
     * @param player The player to add
     * @return A new game if a match was found, null otherwise
     */
    public Game addPlayer(Player player) {
        int boardSize = player.getBoardSize();

        // Initialize the list for this board size if it doesn't exist
        if (!waitingPlayers.containsKey(boardSize)) {
            waitingPlayers.put(boardSize, new ArrayList<>());
        }

        List<Player> playersForSize = waitingPlayers.get(boardSize);

        // If there's already a player waiting, match them
        if (!playersForSize.isEmpty()) {
            Player opponent = playersForSize.remove(0);
            return createGame(opponent, player, boardSize);
        } else {
            // Otherwise, add this player to the waiting list
            playersForSize.add(player);
            notifyPlayerWaiting(player);
            return null;
        }
    }

    /**
     * Create a new game between two players
     * @param player1 First player
     * @param player2 Second player
     * @param boardSize Size of the game board
     * @return The newly created game
     */
    private Game createGame(Player player1, Player player2, int boardSize) {
        // Set symbols for players
        player1.setSymbol('X');
        player2.setSymbol('O');

        // Create and start the game
        Game game = new Game(player1, player2, boardSize);
        activeGames.add(game);

        // Notify observers about the new game
        notifyGameCreated(game);

        return game;
    }

    /**
     * Get a list of all active games
     * @return List of active games
     */
    public List<Game> getActiveGames() {
        return new ArrayList<>(activeGames);
    }

    /**
     * Get a map of all waiting players by board size
     * @return Map of waiting players
     */
    public Map<Integer, List<Player>> getWaitingPlayers() {
        // Return a defensive copy
        Map<Integer, List<Player>> copy = new HashMap<>();
        for (Map.Entry<Integer, List<Player>> entry : waitingPlayers.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * End a game and remove it from active games
     * @param game The game to end
     */
    public void endGame(Game game) {
        activeGames.remove(game);
        notifyGameEnded(game);
    }

    /**
     * Add an observer to listen for manager events
     * @param observer The observer to add
     */
    public void addObserver(ManagerObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove an observer
     * @param observer The observer to remove
     */
    public void removeObserver(ManagerObserver observer) {
        observers.remove(observer);
    }

    // Observer notification methods
    private void notifyPlayerWaiting(Player player) {
        for (ManagerObserver observer : observers) {
            observer.onPlayerWaiting(player);
        }
    }

    private void notifyGameCreated(Game game) {
        for (ManagerObserver observer : observers) {
            observer.onGameCreated(game);
        }
    }

    private void notifyGameEnded(Game game) {
        for (ManagerObserver observer : observers) {
            observer.onGameEnded(game);
        }
    }

    /**
     * Interface for observing manager events
     */
    public interface ManagerObserver {
        void onPlayerWaiting(Player player);
        void onGameCreated(Game game);
        void onGameEnded(Game game);
    }
}