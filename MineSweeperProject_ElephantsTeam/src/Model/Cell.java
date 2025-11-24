package Model;

public class Cell {

    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private boolean isQuestion;
    private boolean isSurprise;
    private int adjacentMines;

    public Cell() {
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.isQuestion = false;
        this.isSurprise = false;
        this.adjacentMines = 0;
    }

    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { this.isMine = mine; }

    public boolean isRevealed() { return isRevealed; }
    public void reveal() { this.isRevealed = true; }

    public boolean isFlagged() { return isFlagged; }
    public void toggleFlag() { if (!isRevealed) this.isFlagged = !this.isFlagged; }

    public boolean isQuestion() { return isQuestion; }
    public void setQuestion(boolean question) { isQuestion = question; }

    public boolean isSurprise() { return isSurprise; }
    public void setSurprise(boolean surprise) { isSurprise = surprise; }

    public int getAdjacentMines() { return adjacentMines; }
    public void setAdjacentMines(int count) { this.adjacentMines = count; }

    public boolean isEmpty() {
        return !isMine && !isQuestion && !isSurprise && adjacentMines == 0;
    }
}