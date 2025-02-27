package com.example.demo;

/**
 * Board class represents the game board for Tic-Tac-Toe.
 * This class is part of the Model in the MVC pattern and contains no UI elements.
 */
public class Board {
    private char[][] grid;
    private final int size;

    /**
     * Constructor for a new board
     * @param size Size of the board (e.g., 3 for a 3x3 board)
     */
    public Board(int size) {
        this.size = size;
        this.grid = new char[size][size];
    }

    /**
     * Initialize the board with empty cells
     */
    public void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = '-';
            }
        }
    }

    /**
     * Make a move on the board
     * @param move The move to make
     * @return 1 if the move results in a win, 0 if it results in a tie, -1 if the game continues
     */
    public int makeMove(Move move) {
        int x = move.getX();
        int y = move.getY();
        char symbol = move.getSymbol();

        // Place the symbol
        grid[x][y] = symbol;

        // Check for win
        if (checkWin(x, y, symbol)) {
            return 1; // Player won
        }

        // Check for tie
        if (isBoardFull()) {
            return 0; // Tie
        }

        return -1; // Game continues
    }

    /**
     * Check if the specified cell is empty
     * @param x Row index
     * @param y Column index
     * @return true if the cell is empty, false otherwise
     */
    public boolean isCellEmpty(int x, int y) {
        return grid[x][y] == '-';
    }

    /**
     * Check if the board is full (tie condition)
     * @return true if the board is full, false otherwise
     */
    private boolean isBoardFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == '-') {
                    return false; // Found an empty cell
                }
            }
        }
        return true; // No empty cells
    }

    /**
     * Check if the last move resulted in a win
     * @param row Row of the last move
     * @param col Column of the last move
     * @param symbol Symbol placed in the last move
     * @return true if the move resulted in a win, false otherwise
     */
    private boolean checkWin(int row, int col, char symbol) {
        // Check row
        boolean rowWin = true;
        for (int j = 0; j < size; j++) {
            if (grid[row][j] != symbol) {
                rowWin = false;
                break;
            }
        }
        if (rowWin) return true;

        // Check column
        boolean colWin = true;
        for (int i = 0; i < size; i++) {
            if (grid[i][col] != symbol) {
                colWin = false;
                break;
            }
        }
        if (colWin) return true;

        // Check main diagonal (only if the move is on the diagonal)
        if (row == col) {
            boolean diagWin = true;
            for (int i = 0; i < size; i++) {
                if (grid[i][i] != symbol) {
                    diagWin = false;
                    break;
                }
            }
            if (diagWin) return true;
        }

        // Check secondary diagonal (only if the move is on the secondary diagonal)
        if (row + col == size - 1) {
            boolean antiDiagWin = true;
            for (int i = 0; i < size; i++) {
                if (grid[i][size - 1 - i] != symbol) {
                    antiDiagWin = false;
                    break;
                }
            }
            if (antiDiagWin) return true;
        }

        return false; // No win
    }

    /**
     * Get the current state of the grid
     * @return A copy of the grid
     */
    public char[][] getGrid() {
        // Return a copy to maintain encapsulation
        char[][] copy = new char[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, size);
        }
        return copy;
    }

    /**
     * Get the size of the board
     * @return The size of the board
     */
    public int getSize() {
        return size;
    }
}