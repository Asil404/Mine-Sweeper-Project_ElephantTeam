package Model;

public enum Difficulty {
    // פורמט: (שורות, עמודות, מוקשים, חיים, שאלות, הפתעות, עלות הפעלה, ניקוד הפתעה)

    // קל: 10 מוקשים, 6 שאלות, 2 הפתעות, 10 חיים, עלות 5, ניקוד הפתעה 8
    EASY(9, 9, 10, 10, 6, 2, 5, 8),

    // בינוני: 26 מוקשים, 7 שאלות, 3 הפתעות, 8 חיים, עלות 8, ניקוד הפתעה 12
    MEDIUM(13, 13, 26, 8, 7, 3, 8, 12),

    // קשה: 44 מוקשים, 11 שאלות, 4 הפתעות, 6 חיים, עלות 12, ניקוד הפתעה 16
    HARD(16, 16, 44, 6, 11, 4, 12, 16);

    private final int rows;
    private final int cols;
    private final int mines;
    private final int lives;
    private final int questions;
    private final int surprises;
    private final int activationCost;
    private final int surpriseReward;

    Difficulty(int rows, int cols, int mines, int lives, int questions, int surprises, int cost, int reward) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.lives = lives;
        this.questions = questions;
        this.surprises = surprises;
        this.activationCost = cost;
        this.surpriseReward = reward;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getMines() { return mines; }
    public int getLives() { return lives; }
    public int getQuestions() { return questions; }
    public int getSurprises() { return surprises; }
    public int getActivationCost() { return activationCost; }
    public int getSurpriseReward() { return surpriseReward; }
}