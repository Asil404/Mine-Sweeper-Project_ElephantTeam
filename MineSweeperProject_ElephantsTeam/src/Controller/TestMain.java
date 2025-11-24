package Controller;

import Model.Difficulty;
import Model.Board;
import Model.Cell;

public class TestMain {
    public static void main(String[] args) {
        GameController controller = new GameController(Difficulty.EASY);
        Board board = controller.getBoard();

        controller.handleLeftClick(3, 3); // like a player click

        // print state after click
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isRevealed()) {
                    if (cell.isMine()) {
                        System.out.print(" * ");
                    } else {
                        System.out.print(" " + cell.getAdjacentMines() + " ");
                    }
                } else if (cell.isFlagged()) {
                    System.out.print(" F ");
                } else {
                    System.out.print(" # ");
                }
            }
            System.out.println();
        }

        System.out.println("Game over: " + controller.isGameOver());
    }
}
