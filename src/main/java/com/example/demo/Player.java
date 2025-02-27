package com.example.demo;

/**
 * Player class represents a player in a Tic-Tac-Toe game.
 * This class is part of the Model in the MVC pattern and contains no UI elements.
 */
public class Player {
    private String name;
    private int boardSize;
    private char symbol;
    private boolean isHuman; // Could be used to distinguish human vs AI players

    /**
     * Constructor for a player
     * @param name Player's name
     * @param boardSize Preferred board size
     */
    public Player(String name, int boardSize) {
        this.name = name;
        this.boardSize = boardSize;
        this.isHuman = true; // Default is human player
    }

    /**
     * Constructor for a player with specified symbol
     * @param name Player's name
     * @param boardSize Preferred board size
     * @param symbol Player's symbol ('X' or 'O')
     */
    public Player(String name, int boardSize, char symbol) {
        this(name, boardSize);
        this.symbol = symbol;
    }

    /**
     * Get the player's name
     * @return Player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the player's name
     * @param name Player's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the preferred board size
     * @return Preferred board size
     */
    public int getBoardSize() {
        return boardSize;
    }

    /**
     * Set the preferred board size
     * @param boardSize Preferred board size
     */
    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    /**
     * Get the player's symbol
     * @return Player's symbol
     */
    public char getSymbol() {
        return symbol;
    }

    /**
     * Set the player's symbol
     * @param symbol Player's symbol
     */
    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    /**
     * Check if the player is human
     * @return true if human, false if AI
     */
    public boolean isHuman() {
        return isHuman;
    }

    /**
     * Set whether the player is human
     * @param isHuman true if human, false if AI
     */
    public void setHuman(boolean isHuman) {
        this.isHuman = isHuman;
    }

    /**
     * String representation of the player
     * @return String with player information
     */
    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}
