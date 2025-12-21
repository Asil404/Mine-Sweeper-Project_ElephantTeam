package Tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Controller.GameController;
import Model.Board;
import Model.Difficulty;

class GameTest {

    private GameController controller;
    private Board board;

    @BeforeEach
    void setUp() {
        // יוצרים קונטרולר בלי View (בלי גרפיקה)
        controller = new GameController(Difficulty.EASY, "P1", "P2");
        board = controller.getBoard1();
    }

    // --- בדיקה 1: האם הלוח נוצר נכון? ---
    @Test
    void test1_BoardInitialization() {
        // ברמה קלה: 9 שורות, 9 עמודות, 10 מוקשים
        assertEquals(9, board.getRows(), "Rows should be 9");
        assertEquals(9, board.getCols(), "Cols should be 9");
        assertEquals(10, board.getMineCount(), "Should start with 10 mines");
    }

    // --- בדיקה 2: האם "לחיצה ראשונה בטוחה" עובדת? ---
    @Test
    void test2_FirstClickSafe() {
        // 1. מוצאים מוקש באופן ידני
        int r = -1, c = -1;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (board.getCell(i, j).isMine()) {
                    r = i; c = j;
                    break;
                }
            }
            if (r != -1) break;
        }

        // 2. לוחצים עליו (בתור המהלך הראשון במשחק)
        controller.handleLeftClick(r, c, 1);

        // 3. בודקים: המוקש היה אמור לזוז משם!
        assertFalse(board.getCell(r, c).isMine(), "Mine should move on first click!");
        assertTrue(board.getCell(r, c).isRevealed(), "Cell should be revealed and safe");
    }

    // --- בדיקה 3: האם דגלים עובדים ומעלים ניקוד? ---
    @Test
    void test3_FlaggingCorrectly() {
        int initialScore = controller.getScore();
        
        // מוצאים מוקש
        int r = 0, c = 0;
        while (!board.getCell(r, c).isMine()) {
            c++;
            if (c >= 9) { c=0; r++; }
        }

        // שמים דגל (קליק ימני)
        controller.handleRightClick(r, c, 1);

        // הניקוד צריך לעלות ב-1
        assertEquals(initialScore + 1, controller.getScore(), "Score should increase by 1 for correct flag");
        assertTrue(board.getCell(r, c).isFlagged(), "Cell should be flagged");
    }

    // --- בדיקה 4: קנס על דגל שגוי (במקום LoseLife) ---
    @Test
    void test4_WrongFlagPenalty() {
        int initialScore = controller.getScore();

        // 1. מוצאים תא שהוא *לא* מוקש (סתם תא ריק)
        int r = 0, c = 0;
        // מחפשים תא שאינו מוקש
        while (board.getCell(r, c).isMine()) {
            c++;
            if (c >= 9) { c=0; r++; }
        }

        // 2. שמים עליו דגל (טעות של השחקן)
        controller.handleRightClick(r, c, 1);

        // 3. לפי חוקי המשחק שלך, דגל שגוי מוריד 3 נקודות
        assertEquals(initialScore - 3, controller.getScore(), "Score should decrease by 3 for wrong flag");
    }
}