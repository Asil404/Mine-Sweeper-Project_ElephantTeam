package Logic;

import View.GameWindow;
import Model.Difficulty; // שים לב לייבוא הזה

public class MinesweeperGame extends AbstractGame {
    
    // משתנים לשמירת המידע מהלוגין
    private Difficulty difficulty;
    private String p1Name, p2Name;
    private String p1Avatar, p2Avatar;
    private GameWindow gameWindow;

    public MinesweeperGame(Difficulty difficulty, String p1Name, String p2Name, String p1Avatar, String p2Avatar) {
        this.difficulty = difficulty;
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.p1Avatar = p1Avatar;
        this.p2Avatar = p2Avatar;
    }

    @Override
    protected void initializeGame() {
        System.out.println("Initializing game for: " + p1Name + " vs " + p2Name);
    }

    @Override
    protected void createWindow() {
        // כאן אנחנו משתמשים בנתונים כדי ליצור את החלון הקיים שלנו
        this.gameWindow = new GameWindow(difficulty, p1Name, p2Name, p1Avatar, p2Avatar);
    }

    @Override
    protected void startGame() {
        this.gameWindow.setVisible(true);
    }
}