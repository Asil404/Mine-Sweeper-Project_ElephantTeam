package Controller;

import Model.*;
import View.GameWindow;
import View.QuestionPopup;
import View.CustomDialog;
import javax.swing.JFrame;
import javax.swing.Timer;
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
    private int secondsPlayed = 0; 
    private Difficulty currentDifficulty;
    private String p1Name, p2Name;
    private JFrame viewFrame; 
    private Random rand = new Random();
    private Timer gameTimer;
    
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
        this.isFirstMove = true; 
        
        SysData.getInstance(); 
        initTimer();
        
    }
    
    public void setViewFrame(JFrame frame) { 
        this.viewFrame = frame; 
        updateViewHUD();
    }

    private void initTimer() {
        gameTimer = new Timer(1000, e -> {
            if (!isPaused && !gameOver) {
                secondsPlayed++;
                if (viewFrame instanceof GameWindow) {
                    String timeStr = String.format("%02d:%02d", secondsPlayed / 60, secondsPlayed % 60);
                    ((GameWindow) viewFrame).updateTimer(timeStr);
                }
            }
        });
        gameTimer.start();
    }

    public void togglePause() { isPaused = !isPaused; }
    public boolean isPaused() { return isPaused; }

    private void updateViewHUD() {
        if (viewFrame instanceof GameWindow) {
            GameWindow gw = (GameWindow) viewFrame;
            gw.updateScore(score);
            gw.updateLives(lives);
        }
    }
    
    public int getProgress() {
        int totalSafe = (board1.getRows() * board1.getCols() - board1.getMineCount()) * 2;
        int revealed = countRevealedSafe(board1) + countRevealedSafe(board2);
        if (totalSafe == 0) return 0;
        return (int) ((double) revealed / totalSafe * 100);
    }

    public void handleLeftClick(int row, int col, int playerID) {
        if (gameOver || isPaused) return;
        if ((playerID == 1 && !isPlayer1Turn) || (playerID == 2 && isPlayer1Turn)) return;

        Board currentBoard = (playerID == 1) ? board1 : board2;
        
        if (isFirstMove) {
            currentBoard.ensureSafeStart(row, col); 
            isFirstMove = false; 
        }

        Cell cell = currentBoard.getCell(row, col);
        if (cell.isFlagged()) return; 


        // Special Features
        if (cell.isRevealed()) {
            if (!cell.isUsed() && (cell.isQuestion() || cell.isSurprise())) {
                handleSpecialFeature(cell, currentBoard);
            }
            return; 
        }

        // חשיפה וניקוד
        int pointsEarned = currentBoard.revealCell(row, col);

        if (cell.isMine()) {
            lives--; 
            if (viewFrame instanceof GameWindow) ((GameWindow) viewFrame).triggerShakeEffect();
            checkGameOver(); // <--- כאן מתבצעת הבדיקה אם הפסדנו
        } else {
            score += pointsEarned; 
        }
        
        updateViewHUD();
        checkVictory(); 
        
        if (!gameOver) {
            if (isBoardCleared(currentBoard)) forceSwitchTurn();
            else switchTurn();
        }
    }

    private void handleSpecialFeature(Cell cell, Board currentBoard) {
        int cost = currentDifficulty.getActivationCost();
        if (score < cost) {
            if (viewFrame != null) CustomDialog.showMessage(viewFrame, "Insufficient Points", "Need " + cost + " points!");
            return;
        }
        if (viewFrame != null) {
            int choice = CustomDialog.showConfirm(viewFrame, "Activate?", "Cost: " + cost + " points.");
            if (choice != CustomDialog.YES_OPTION) return;
        }

        score -= cost;
        if (cell.isQuestion()) handleQuestion(cell, currentBoard);
        else handleSurprise(cell);
        
        cell.setUsed(true); 
        updateViewHUD();
        if (viewFrame instanceof GameWindow) ((GameWindow) viewFrame).updateStats(); 
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
        
        updateViewHUD();
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
        if (!correct) {
            cell.setQuestionWrong(true);
        }
        // 1. Snapshot values BEFORE logic
        int scoreBefore = score;
        int livesBefore = lives;

        // 2. Apply Logic
        applyQuestionLogic(correct, qLevel, board);

        // 3. Snapshot values AFTER logic
        int scoreDiff = score - scoreBefore;
        int livesDiff = lives - livesBefore;

        // 4. Show Feedback to the user
        showFeedbackMessage(correct, scoreDiff, livesDiff);

        updateViewHUD();
        checkGameOver();
        checkVictory();
    }
    private void showFeedbackMessage(boolean correct, int scoreChange, int livesChange) {
        if (viewFrame == null) return;

        StringBuilder msg = new StringBuilder();
        msg.append(correct ? "Correct Answer!" : "Wrong Answer!");
        msg.append("\n");

        // Format Score part
        if (scoreChange > 0) msg.append("Score: +").append(scoreChange);
        else if (scoreChange < 0) msg.append("Score: ").append(scoreChange); // includes '-'
        else msg.append("Score: No Change");

        // Format Lives part
        if (livesChange != 0) {
            msg.append("\nLives: ").append(livesChange > 0 ? "+" : "").append(livesChange);
        }

        String title = correct ? "Well Done!" : "Ouch!";
        CustomDialog.showMessage(viewFrame, title, msg.toString()); }
    
    
    private void applyQuestionLogic(boolean correct, String qLevel, Board board) {
        if (currentDifficulty == Difficulty.EASY) {
            switch (qLevel) {
                // CHANGED: Increased from 3 to 5 so you don't lose points (Cost 5 - Reward 5 = 0)
                // You gain a life, so it's still worth it.
                case "Easy": 
                    if (correct) { score += 5; addLives(1); } 
                    else { if (rand.nextBoolean()) score -= 3; } 
                    break;
                case "Medium": 
                    if (correct) { score += 6; revealRandomMine(board); } 
                    else { if (rand.nextBoolean()) score -= 6; } 
                    break;
                case "Hard": 
                    if (correct) { score += 10; revealArea3x3(board); } 
                    else { score -= 10; } 
                    break;
                case "Expert": 
                    if (correct) { score += 15; addLives(2); } 
                    else { score -= 15; lives--; } 
                    break;
            }
        } else if (currentDifficulty == Difficulty.MEDIUM) {
             switch (qLevel) {
                case "Easy": if (correct) { score += 8; addLives(1); } else { score -= 8; } break;
                case "Medium": if (correct) { score += 10; addLives(1); } else { if (rand.nextBoolean()) { score -= 10; lives--; } } break;
                case "Hard": if (correct) { score += 15; addLives(1); } else { score -= 15; lives--; } break;
                case "Expert": if (correct) { score += 20; addLives(2); } else { if (rand.nextBoolean()) { score -= 20; lives--; } else { score -= 20; lives -= 2; } } break;
            }
        } else { // HARD
             switch (qLevel) {
                case "Easy": if (correct) { score += 10; addLives(1); } else { score -= 10; lives--; } break;
                case "Medium": if (correct) { if (rand.nextBoolean()) { score += 15; addLives(1); } else { score += 15; addLives(2); } } else { if (rand.nextBoolean()) { score -= 15; lives--; } else { score -= 15; lives -= 2; } } break;
                case "Hard": if (correct) { score += 20; addLives(2); } else { score -= 20; lives -= 2; } break;
                case "Expert": if (correct) { score += 40; addLives(3); } else { score -= 40; lives -= 3; } break;
            }
        }
    }

    private void handleSurprise(Cell cell) {
        boolean isGood = rand.nextBoolean(); 
        int pointsEffect = currentDifficulty.getSurpriseReward(); 
        
        if (isGood) {
            addLives(1); score += pointsEffect;
            if (viewFrame != null) CustomDialog.showMessage(viewFrame, "GIFT :)", "+1 Life, +" + pointsEffect + " Score");
        } else {
            lives--; score -= pointsEffect;
            if (viewFrame instanceof GameWindow) ((GameWindow) viewFrame).triggerShakeEffect();
            if (viewFrame != null) CustomDialog.showMessage(viewFrame, "TRAP :(", "-1 Life, -" + pointsEffect + " Score");
        }
        updateViewHUD();
        checkGameOver();
    }

    private void addLives(int amount) {
        int overflowValue = currentDifficulty.getActivationCost();
        for (int i = 0; i < amount; i++) {
            if (lives < 10) lives++; else score += overflowValue; 
        }
        updateViewHUD();
    }

    // --- התיקון הגדול נמצא כאן: שמירת היסטוריה בהפסד ---
    private void checkGameOver() { 
        if (lives <= 0) { 
            lives = 0; 
            gameOver = true;
            revealAllBoards(); 
            
            // !!! שומרים את ההפסד להיסטוריה !!!
            GameRecord record = new GameRecord(
                p1Name, p2Name, "Computer", score, currentDifficulty.toString()
            );
            HistoryManager.getInstance().addRecord(record);
            // ------------------------------------------

            if (viewFrame != null) {
                // דיאלוג הסיום (יפתח אחרי שנייה כדי שהשחקן יראה את הלוח)
                // הלוגיקה של פתיחת החלון נמצאת בתוך ה-GameWindow בטיימר
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

    // --- שמירת היסטוריה בניצחון ---
    private void finalizeGame() {
        if (lives > 0) {
            int activationCost = currentDifficulty.getActivationCost();
            int bonusPoints = lives * activationCost;
            score += bonusPoints;
        }
        revealAllBoards();
        
        

        String winnerName = (score > 0) ? p1Name : (p2Name.equals("Computer") ? "Computer" : p2Name); 

        // שמירה בניצחון (היה קיים כבר)
        GameRecord record = new GameRecord(
            p1Name, p2Name, winnerName, score, currentDifficulty.toString()
        );
        HistoryManager.getInstance().addRecord(record);
    }
    
    private void revealAllBoards() {
        revealBoard(board1);
        revealBoard(board2);
        if(viewFrame != null) viewFrame.repaint();
    }
    
    private void revealBoard(Board b) {
        for(int r=0; r<b.getRows(); r++){
            for(int c=0; c<b.getCols(); c++){
                b.getCell(r,c).setRevealed(true);
            }
        }
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

    public void handleChatMessage(String message) {
    }

    public String getCurrentPlayerName() { return isPlayer1Turn ? p1Name : p2Name; }
    public Board getBoard1() { return board1; }
    public Board getBoard2() { return board2; }
    public boolean isPlayer1Turn() { return isPlayer1Turn; }
    public boolean isGameOver() { return gameOver; }
    public boolean isVictory() { return victory; } 
    public int getLives() { return lives; }
    public int getScore() { return score; }
}