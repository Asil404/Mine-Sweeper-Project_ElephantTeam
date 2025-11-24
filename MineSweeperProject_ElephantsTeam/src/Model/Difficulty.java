package Model;

public enum Difficulty {
    // Rows, Cols, Mines, Questions, Surprises
    EASY(9, 9, 10, 6, 2),
    MEDIUM(13, 13, 26, 7, 3),
    HARD(16, 16, 44, 11, 4);

    private final int rows;
    private final int cols;
    private final int mines;
    private final int questions;
    private final int surprises;

    Difficulty(int rows, int cols, int mines, int questions, int surprises) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.questions = questions;
        this.surprises = surprises;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getMines() { return mines; }
    public int getQuestions() { return questions; }
    public int getSurprises() { return surprises; }
}