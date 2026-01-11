package Logic;

import Model.Difficulty;

public class GameFactory {
    
    public AbstractGame createGame(Difficulty difficulty, String p1Name, String p2Name, String p1Avatar, String p2Avatar) {
        return new MinesweeperGame(difficulty, p1Name, p2Name, p1Avatar, p2Avatar);
    }
}