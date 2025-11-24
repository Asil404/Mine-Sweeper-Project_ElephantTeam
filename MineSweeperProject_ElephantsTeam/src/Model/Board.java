package Model;

import java.util.Random;

public class Board {

    private final int rows;
    private final int cols;
    private final int mineCount;
    private final int questionCount;
    private final int surpriseCount;
    private final Cell[][] grid;

    public Board(Difficulty lvl) {
        this.rows = lvl.getRows();
        this.cols = lvl.getCols();
        this.mineCount = lvl.getMines();
        this.questionCount = lvl.getQuestions();
        this.surpriseCount = lvl.getSurprises();
        this.grid = new Cell[rows][cols];

        initializeBoard();
        placeMinesRandomly();
        calculateAllAdjacentMines();
        placeSpecialCells();
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

    private void placeSpecialCells() {
        Random rand = new Random();
        int placedQ = 0;
        while (placedQ < questionCount) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            Cell cell = grid[r][c];
            if (!cell.isMine() && !cell.isQuestion() && !cell.isSurprise()) {
                cell.setQuestion(true);
                placedQ++;
            }
        }
        int placedS = 0;
        while (placedS < surpriseCount) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            Cell cell = grid[r][c];
            if (!cell.isMine() && !cell.isQuestion() && !cell.isSurprise()) {
                cell.setSurprise(true);
                placedS++;
            }
        }
    }

    private void calculateAllAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!grid[r][c].isMine()) {
                    grid[r][c].setAdjacentMines(countAdjacentMines(r, c));
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (!inBounds(r, c) || (r == row && c == col)) continue;
                if (grid[r][c].isMine()) count++;
            }
        }
        return count;
    }

    public void revealCell(int row, int col) {
        if (!inBounds(row, col)) return;
        Cell cell = grid[row][col];
        if (cell.isRevealed() || cell.isFlagged()) return;

        cell.reveal();

        // Recursion only for empty cells
        if (cell.isEmpty()) {
            cascadeReveal(row, col);
        }
    }

    private void cascadeReveal(int row, int col) {
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r == row && c == col) continue;
                if (inBounds(r, c)) {
                    Cell neighbor = grid[r][c];
                    if (!neighbor.isRevealed() && !neighbor.isFlagged() && !neighbor.isMine()) {
                        neighbor.reveal();
                        if (neighbor.isEmpty()) {
                            cascadeReveal(r, c);
                        }
                    }
                }
            }
        }
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public Cell getCell(int r, int c) { return grid[r][c]; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public void toggleFlag(int r, int c) { if (inBounds(r, c)) grid[r][c].toggleFlag(); }
}