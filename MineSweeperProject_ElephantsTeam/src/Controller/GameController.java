package Controller;

import Model.Board;
import Model.Difficulty;
import Model.Cell;

public class GameController {

    private Board board1; // Player 1
    private Board board2; // Player 2
    private boolean isPlayer1Turn;
    // gameOver is kept just to stop clicks, but no "You Lose" logic is required yet.
    private boolean gameOver; 

    public GameController(Difficulty difficulty) {
        this.board1 = new Board(difficulty);
        this.board2 = new Board(difficulty);
        this.isPlayer1Turn = true; // Player 1 starts
        this.gameOver = false;
    }

    public void handleLeftClick(int row, int col, int playerID) {
        if (gameOver) return;

        // Enforce Turn Rules
        if (playerID == 1 && !isPlayer1Turn) return;
        if (playerID == 2 && isPlayer1Turn) return;

        Board currentBoard = (playerID == 1) ? board1 : board2;
        Cell cell = currentBoard.getCell(row, col);

        if (cell.isRevealed() || cell.isFlagged()) return;

        currentBoard.revealCell(row, col);

        // Simple turn switch logic
        // (If you want hitting a mine to STOP the game, you can set gameOver = true here)
        if (cell.isMine()) {
             System.out.println("Mine Hit!"); 
             // gameOver = true; // Optional for Iteration 1
        }
        
        // Iteration 1: Just switch turns, no complex effects
        isPlayer1Turn = !isPlayer1Turn;
    }

    public void handleRightClick(int row, int col, int playerID) {
        if (gameOver) return;
        
        if (playerID == 1 && !isPlayer1Turn) return;
        if (playerID == 2 && isPlayer1Turn) return;

        Board currentBoard = (playerID == 1) ? board1 : board2;
        currentBoard.toggleFlag(row, col);
    }

    public Board getBoard1() { return board1; }
    public Board getBoard2() { return board2; }
    public boolean isPlayer1Turn() { return isPlayer1Turn; }
    public boolean isGameOver() { return gameOver; }
}