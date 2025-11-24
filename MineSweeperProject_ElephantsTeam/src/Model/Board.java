package Model;

import java.util.Random;

public class Board {

    private final int rows;
    private final int cols;
    private final int mineCount;
    private final Cell[][] grid;

    // Constructor based on difficulty level
    public Board(Difficulty lvl) {
        this.rows = lvl.getRows();
        this.cols = lvl.getCols();
        this.mineCount = lvl.getMines();
        this.grid = new Cell[rows][cols];

        initializeBoard();
        placeMinesRandomly();
        calculateAllAdjacentMines();
    }

    private void initializeBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }
    }

    private void placeMinesRandomly() {
        Random rand = new Random();
        int placed = 0;

        while (placed < mineCount) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);

            if (!grid[r][c].isMine()) {
                grid[r][c].setMine(true);
                placed++;
            }
        }
    }

    private void calculateAllAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!grid[r][c].isMine()) {
                    int count = countAdjacentMines(r, c);
                    grid[r][c].setAdjacentMines(count);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;

        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                // Skip out-of-bounds and the center cell itself
                if (!inBounds(r, c) || (r == row && c == col)) {
                    continue;
                }

                if (grid[r][c].isMine()) {
                    count++;
                }
            }
        }

        return count;
    }

    public Cell getCell(int r, int c) {
        return grid[r][c];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    /**
     * Reveal logic for a single cell (including cascade for empty cells).
     */
    public void revealCell(int row, int col) {
        if (!inBounds(row, col)) {
            return;
        }

        Cell cell = grid[row][col];

        // If already revealed or flagged → do nothing
        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        // Reveal this cell
        cell.reveal();

        // If it's a mine → stop (controller will handle life / game over)
        if (cell.isMine()) {
            return;
        }

        // If no adjacent mines → cascade
        if (cell.isEmpty()) {
            cascadeReveal(row, col);
        }
    }

    /**
     * Recursively reveal neighboring empty cells and their borders,
     * without revealing mines.
     */
    private void cascadeReveal(int row, int col) {
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {

                // Skip the center cell
                if (r == row && c == col) {
                    continue;
                }

                if (!inBounds(r, c)) {
                    continue;
                }

                Cell neighbor = grid[r][c];

                // Skip already revealed or flagged cells
                if (neighbor.isRevealed() || neighbor.isFlagged()) {
                    continue;
                }

                // Do not reveal mines during cascade
                if (neighbor.isMine()) {
                    continue;
                }

                // Reveal neighbor
                neighbor.reveal();

                // If neighbor is empty → keep cascading
                if (neighbor.isEmpty()) {
                    cascadeReveal(r, c);
                }
            }
        }
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public void toggleFlag(int row, int col) {
        if (!inBounds(row, col)) {
            return;
        }

        Cell cell = grid[row][col];
        cell.toggleFlag();
    }
}
