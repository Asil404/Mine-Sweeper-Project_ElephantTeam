package Model;

import java.io.Serializable; 


public class GameRecord implements Serializable {
    
  
    private static final long serialVersionUID = 1L; 

    private String p1Name;
    private String p2Name;
    private String winner;
    private int score;
    private String difficulty;
    private String date;

    public GameRecord(String p1, String p2, String winner, int score, String diff) {
        this.p1Name = p1;
        this.p2Name = p2;
        this.winner = winner;
        this.score = score;
        this.difficulty = diff;
        this.date = java.time.LocalDate.now().toString(); // תאריך נוכחי
    }

    // Getters
    public String getP1Name() { return p1Name; }
    public String getP2Name() { return p2Name; }
    public String getWinner() { return winner; }
    public int getScore() { return score; }
    public String getDifficulty() { return difficulty; }
    public String getDate() { return date; }
}
