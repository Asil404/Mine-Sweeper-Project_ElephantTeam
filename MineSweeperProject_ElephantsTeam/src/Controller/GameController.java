package Controller;

import Model.*;
import View.GameWindow;
import View.QuestionPopup;
import View.CustomDialog;  
import javax.swing.JFrame;
import java.util.Random;

public class GameController {

    private Board board1;
    private Board board2;
    private boolean isPlayer1Turn;
    private boolean gameOver;
    private boolean victory;
    private boolean isPaused = false;
    private int lives;
    private int score;
    private Difficulty currentDifficulty;
    private String p1Name, p2Name;
    private JFrame viewFrame; 
    private Random rand = new Random();
    
    // משתנה לניהול לחיצה ראשונה בטוחה
    private boolean isFirstMove = true;

    public GameController(Difficulty difficulty, String p1Name, String p2Name) {
        this.currentDifficulty = difficulty;
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        
        this.board1 = new Board(difficulty);
        this.board2 = new Board(difficulty);
        
        this.lives = difficulty.getLives(); 
        this.score = 0;
        this.isPlayer1Turn = true; 
        this.isFirstMove = true; // אתחול מהלך ראשון
        
        SysData.getInstance(); 
    }
    
    public void setViewFrame(JFrame frame) { this.viewFrame = frame; }
    public void togglePause() { isPaused = !isPaused; }
    public boolean isPaused() { return isPaused; }

    public int getProgress() {
        int totalSafe = (board1.getRows() * board1.getCols() - board1.getMineCount()) * 2;
        int revealed = countRevealedSafe(board1) + countRevealedSafe(board2);
        if (totalSafe == 0) return 0;
        return (int) ((double) revealed / totalSafe * 100);
    }

    private void addLives(int amount) {
        int overflowValue = currentDifficulty.getActivationCost();
        for (int i = 0; i < amount; i++) {
            if (lives < 10) {
                lives++;
            } else {
                score += overflowValue; 
            }
        }
    }

    public void handleLeftClick(int row, int col, int playerID) {
        if (gameOver || isPaused) return;
        if ((playerID == 1 && !isPlayer1Turn) || (playerID == 2 && isPlayer1Turn)) return;

        Board currentBoard = (playerID == 1) ? board1 : board2;
        
        // --- בדיקת מהלך ראשון (First Click Safe) ---
        if (isFirstMove) {
            currentBoard.ensureSafeStart(row, col); // הזזת מוקש אם צריך
            isFirstMove = false; // מעכשיו המשחק רגיל
        }
        // -------------------------------------------

        Cell cell = currentBoard.getCell(row, col);

        if (cell.isFlagged()) return; 

        // --- טיפול בהפעלה (לחיצה שנייה על משבצת פתוחה) ---
     // בתוך הפונקציה handleLeftClick
     // ...
     if (cell.isRevealed()) {
         if (!cell.isUsed()) {
             if (cell.isQuestion() || cell.isSurprise()) {
                 
                 int cost = currentDifficulty.getActivationCost();

                 // --- התיקון: בדיקה שיש מספיק נקודות לפני שממשיכים ---
                 if (score < cost) {
                     if (viewFrame != null) {
                         // שימוש ב-CustomDialog להודעת שגיאה יפה
                         CustomDialog.showMessage(viewFrame, 
                             "Insufficient Points", 
                             "You need " + cost + " points to use this feature!<br>Earn more points by revealing cells.");
                     }
                     return; // יוצאים מהפונקציה ולא מבצעים את הפעולה
                 }
                 // -----------------------------------------------------

                 // --- דיאלוג אישור (נשאר כמו שהיה) ---
                 if (viewFrame != null) {
                     String type = cell.isQuestion() ? "Question" : "Surprise Box";
                     String msg = "Activating this " + type + " costs " + cost + " points.<br>Do you want to proceed?";
                     
                     int choice = CustomDialog.showConfirm(viewFrame, "Confirm Activation", msg);
                     
                     if (choice != CustomDialog.YES_OPTION) {
                         return; // ביטול
                     }
                 }

                 score -= cost; // עכשיו בטוח להוריד כי בדקנו שיש מספיק
                 
                 if (cell.isQuestion()) handleQuestion(cell, currentBoard);
                 else handleSurprise(cell);
                 
                 cell.setUsed(true); 
                 
                 // עדכון ה-HUD (מומלץ לוודא שקורה ריענון לתצוגה)
                 if (viewFrame instanceof GameWindow) {
                    ((GameWindow) viewFrame).updateStats(); 
                 }
             }
         }
         return; 
     }
     // ...

        // --- חשיפה רגילה ---
        currentBoard.revealCell(row, col);

        if (cell.isMine()) {
            lives--; 
            if (viewFrame instanceof GameWindow) ((GameWindow) viewFrame).triggerShakeEffect();
            checkGameOver();
        } else {
            score += 1; 
        }
        
        checkVictory(); 
        
        if (!gameOver) {
            if (isBoardCleared(currentBoard)) forceSwitchTurn();
            else switchTurn();
        }
    }

    public void handleRightClick(int row, int col, int playerID) {
        if (gameOver || isPaused) return;
        if ((playerID == 1 && !isPlayer1Turn) || (playerID == 2 && isPlayer1Turn)) return;

        Board currentBoard = (playerID == 1) ? board1 : board2;
        Cell cell = currentBoard.getCell(row, col);

        if (cell.isRevealed()) return; 

        currentBoard.toggleFlag(row, col);

        if (cell.isFlagged()) {
            if (cell.isMine()) {
                score += 1;
                cell.setRevealed(true); 
            } else {
                score -= 3; 
            }
        } else {
            if (cell.isMine()) score -= 1; 
        }
        
        checkVictory();
        
        if (!gameOver && isBoardCleared(currentBoard)) {
            forceSwitchTurn();
        }
    }
    
    private void handleQuestion(Cell cell, Board board) {
        Question q = SysData.getInstance().getQuestionByLevel(currentDifficulty);
        if (q == null || viewFrame == null) return;

        QuestionPopup popup = new QuestionPopup(viewFrame, q);
        popup.setVisible(true); 
        
        boolean correct = popup.isAnswerCorrect();
        String qLevel = q.getLevel(); 
        if (qLevel == null) qLevel = "Medium"; 

        if (!correct) cell.setQuestionWrong(true); 

        applyQuestionLogic(correct, qLevel, board);
        checkGameOver();
        checkVictory();
    }
    
    private void applyQuestionLogic(boolean correct, String qLevel, Board board) {
        if (currentDifficulty == Difficulty.EASY) {
            switch (qLevel) {
                case "Easy": if (correct) {
                	score += 3; addLives(1); }
                else { if (rand.nextBoolean()) score -= 3; }
                break;
                
                case "Medium": if (correct) { 
                	score += 6; revealRandomMine(board); }
                else { if (rand.nextBoolean()) score -= 6;
                } break;
                
                case "Hard": if (correct) {
                	score += 10; revealArea3x3(board); } 
                else { score -= 10; }
                break;
                
                case "Expert": if (correct) {
                	score += 15; addLives(2); }
                else { score -= 15; lives--; }
                break;
            }
        } else if (currentDifficulty == Difficulty.MEDIUM) {
            switch (qLevel) {
                case "Easy": if (correct) {
                	score += 8; addLives(1); }
                else { score -= 8; } 
                break;
                case "Medium": if (correct) {
                	score += 10; addLives(1); 
                	} 
                else { if (rand.nextBoolean()) {
                	score -= 10; lives--; } } break;
                case "Hard": if (correct) {
                	score += 15; addLives(1); } 
                else { score -= 15; lives--;
                } break;
                case "Expert": if (correct) {
                	score += 20; addLives(2); } else {
                		if (rand.nextBoolean()) { score -= 20; lives--; } 
                		else { score -= 20; lives -= 2; } 
                		} break;
            }
        } else { // HARD
            switch (qLevel) {
                case "Easy": if (correct) {
                	score += 10; addLives(1); } 
                else { score -= 10; lives--; } 
                break;
                case "Medium": if (correct) {
                	if (rand.nextBoolean()) {
                		score += 15; addLives(1); } 
                	else { score += 15; addLives(2); } 
                	} else { if (rand.nextBoolean()) {
                		score -= 15; lives--; } else {
                			score -= 15; lives -= 2; } 
                	} break;
                case "Hard": if (correct) {
                	score += 20; addLives(2); 
                	} else { score -= 20; lives -= 2; } 
                break;
                case "Expert": if (correct) { 
                	score += 40; addLives(3); }
                else { score -= 40; lives -= 3; }
                break;
            }
        }
    }

    private void handleSurprise(Cell cell) {
        boolean isGood = rand.nextBoolean(); // 50-50
        int pointsEffect = currentDifficulty.getSurpriseReward(); 

        String message = ""; 
        String title = ""; 
        
        if (isGood) {
            addLives(1);
            score += pointsEffect;
            title = "LUCKY GIFT! :) ";
            message = "You found a medkit!<br>+1 Life ❤️<br>+" + pointsEffect + " Score 🏆";
        } else {
            lives--; 
            score -= pointsEffect;
            title = "BAD GIFT! :(";
            message = "It was a trap!<br>-1 Life 💔<br>-" + pointsEffect + " Score 📉";
            
            if (viewFrame instanceof GameWindow) ((GameWindow) viewFrame).triggerShakeEffect();
        }
        
        if (viewFrame != null) {
            CustomDialog.showMessage(viewFrame, title, message);
        }
        
        checkGameOver();
    }

    private void revealRandomMine(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isMine() && !cell.isRevealed()) {
                    cell.setRevealed(true); return; 
                }
            }
        }
    }

    private void revealArea3x3(Board board) {
        int r = rand.nextInt(board.getRows());
        int c = rand.nextInt(board.getCols());
        for (int i = r - 1; i <= r + 1; i++) {
            for (int j = c - 1; j <= c + 1; j++) {
                if (i >= 0 && i < board.getRows() && j >= 0 && j < board.getCols()) {
                    Cell cell = board.getCell(i, j);
                    if (!cell.isRevealed() && !cell.isFlagged()) {
                        board.revealCell(i, j); 
                        if (!cell.isMine()) score += 1; 
                    }
                }
            }
        }
    }

    private void checkVictory() {
        if (gameOver) return;
        boolean p1Win = isBoardCleared(board1);
        boolean p2Win = isBoardCleared(board2);

        if (p1Win || p2Win) {
            victory = true;
            gameOver = true;
            finalizeGame(); 
        }
    }
    
    private void checkGameOver() { 
        if (lives <= 0) { 
            lives = 0; 
            gameOver = true;
            revealAllBoards(); 
        } 
    }

    private void finalizeGame() {
        if (lives > 0) {
            int activationCost = currentDifficulty.getActivationCost();
            int bonusPoints = lives * activationCost;
            score += bonusPoints;
        }
        revealAllBoards();
    }
    
    private void revealAllBoards() {
        revealBoard(board1);
        revealBoard(board2);
    }
    
    private void revealBoard(Board b) {
        for(int r=0; r<b.getRows(); r++){
            for(int c=0; c<b.getCols(); c++){
                b.getCell(r,c).setRevealed(true);
            }
        }
    }

    private boolean isBoardCleared(Board b) {
        int totalSafeCells = (b.getRows() * b.getCols()) - b.getMineCount();
        return countRevealedSafe(b) >= totalSafeCells;
    }
    
    private int countRevealedSafe(Board b) {
        int count = 0;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                Cell cell = b.getCell(r, c);
                if (cell.isRevealed() && !cell.isMine()) count++;
            }
        }
        return count;
    }

    private void switchTurn() {
        if (!gameOver) {
            boolean p1Done = isBoardCleared(board1);
            boolean p2Done = isBoardCleared(board2);
            if (isPlayer1Turn) { if (!p2Done) isPlayer1Turn = false; } 
            else { if (!p1Done) isPlayer1Turn = true; }
        }
    }

    private void forceSwitchTurn() {
        if (isPlayer1Turn) { if (!isBoardCleared(board2)) isPlayer1Turn = false; } 
        else { if (!isBoardCleared(board1)) isPlayer1Turn = true; }
    }

    public String getCurrentPlayerName() {
    	return isPlayer1Turn ? p1Name : p2Name; }
    
    public Board getBoard1() { 
    	return board1; }
    
    public Board getBoard2() { 
    	return board2; }
    
    public boolean isPlayer1Turn() {
    	return isPlayer1Turn; }
    
    public boolean isGameOver() {
    	return gameOver; }
    
    public boolean isVictory() { 
    	return victory; } 
    
    public int getLives() { 
    	return lives; }
    
    public int getScore() { 
    	return score; }
}