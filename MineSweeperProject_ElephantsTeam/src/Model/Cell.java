package Model;

public class Cell {
    private int row, col;
    private boolean isMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;
    
    private boolean isQuestion;
    private boolean isSurprise;
    private boolean isQuestionWrong; 
    
    private boolean isUsed = false; 

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void reveal() { this.isRevealed = true; this.isFlagged = false; }
    public void toggleFlag() { this.isFlagged = !isFlagged; }

    // Getters & Setters
    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }

    public boolean isRevealed() { return isRevealed; }
    public void setRevealed(boolean revealed) { isRevealed = revealed; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public int getAdjacentMines() { return adjacentMines; }
    public void setAdjacentMines(int adjacentMines) { this.adjacentMines = adjacentMines; }
    
    public boolean isQuestion() { return isQuestion; }
    public void setQuestion(boolean q) { this.isQuestion = q; }
    
    public boolean isSurprise() { return isSurprise; }
    public void setSurprise(boolean s) { this.isSurprise = s; }
    
    public boolean isQuestionWrong() { return isQuestionWrong; }
    public void setQuestionWrong(boolean w) { this.isQuestionWrong = w; }
    
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { this.isUsed = used; }
}