package Controller;

import Model.Board;
import Model.Difficulty;
import Model.Cell;

public class GameController {

    private Board board;
    private boolean gameOver;

    public GameController(Difficulty difficulty) {
        this.board = new Board(difficulty);
        this.gameOver = false;
    }

    // Left click: reveal a cell
    public void handleLeftClick(int row, int col) {
        if (gameOver) {
            return;
        }

        Cell cell = board.getCell(row, col);

        // If already revealed or flagged – do nothing
        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        board.revealCell(row, col);

        if (cell.isMine()) {
            // For now just mark game as over – later View will show message
            gameOver = true;
        }
        // Later: we’ll notify the View to refresh here
    }

    // Right click: toggle flag
    public void handleRightClick(int row, int col) {
        if (gameOver) {
            return;
        }
        board.toggleFlag(row, col);
        // Later: View refresh
    }

    // Getter so the View can read the board state
    public Board getBoard() {
        return board;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
