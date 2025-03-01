package com.example.demo;

/**
 * Controller for the lobby/matchmaking functionality
 */
public class LobbyController
{
    private Manager manager;

    /**
     * Constructor for the lobby controller
     * @param manager The game manager
     */
    public LobbyController(Manager manager) {
        this.manager = manager;
    }

    /**
     * Register a player and try to match them with an opponent
     * @param player The player to register
     * @return A game if the player was matched, null otherwise
     */
    public Game registerPlayer(Player player) {
        return manager.addPlayer(player);
    }

    /**
     * Cancel a player's registration
     * @param player The player to deregister
     */
    public void cancelRegistration(Player player) {
        // This would need to be implemented in the Manager class
        // manager.removeWaitingPlayer(player);
    }

    /**
     * Check if a player is waiting for a match
     * @param player The player to check
     * @return true if the player is waiting, false otherwise
     */
    public boolean isPlayerWaiting(Player player) {
        return manager.getWaitingPlayers().values().stream()
                .anyMatch(list -> list.contains(player));
    }
}