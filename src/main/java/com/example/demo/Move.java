package com.example.demo;

/**
 * Move class represents a single move in a Tic-Tac-Toe game.
 * This class is part of the Model in the MVC pattern.
 */
public class Move {
    private int x;
    private int y;
    private char symbol;

    /**
     * Constructor for a move
     * @param x Row index
     * @param y Column index
     * @param symbol Player's symbol ('X' or 'O')
     */
    public Move(int x, int y, char symbol) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
    }

    /**
     * Get the row index
     * @return Row index
     */
    public int getX() {
        return x;
    }

    /**
     * Set the row index
     * @param x Row index
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get the column index
     * @return Column index
     */
    public int getY() {
        return y;
    }

    /**
     * Set the column index
     * @param y Column index
     */
    public void setY(int y) {
        this.y = y;
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
     * String representation of the move
     * @return String in format "(row,col):symbol"
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + "):" + symbol;
    }
}