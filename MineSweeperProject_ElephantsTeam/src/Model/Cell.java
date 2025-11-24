package Model;

public class Cell {

    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;

    public Cell() {
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }

    // --- Getters & Setters ---

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        this.isMine = mine;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public void reveal() {
        this.isRevealed = true;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int count) {
        this.adjacentMines = count;
    }

    /**
     * Toggle flag only if the cell is not revealed.
     */
    public void toggleFlag() {
        if (!isRevealed) {
            this.isFlagged = !this.isFlagged;
        }
    }

    /**
     * Convenience method to check if this is a non-mine cell
     * with no adjacent mines (used for cascade).
     */
    public boolean isEmpty() {
        return !isMine && adjacentMines == 0;
    }
}
