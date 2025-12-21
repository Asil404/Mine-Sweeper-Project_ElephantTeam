package Model;

import java.util.Random;

public class Board {
    private final int rows;
    private final int cols;
    private final int mineCount;
    private final int questionCount;
    private final int surpriseCount;
    private final Cell[][] grid;

    public Board(Difficulty lvl) {
        this.rows = lvl.getRows();
        this.cols = lvl.getCols();
        this.mineCount = lvl.getMines();
        this.questionCount = lvl.getQuestions();
        this.surpriseCount = lvl.getSurprises();
        this.grid = new Cell[rows][cols];

        initializeBoard();
        
        // 1. פיזור מוקשים אקראי
        placeMinesRandomly();
        
        // 2. חישוב מספרים (שכנים)
        calculateAllAdjacentMines(); 
        
        // 3 + 4. פיזור שאלות והפתעות (רק על משבצות ריקות!)
        placeSpecialCells(); 
    }

    private void initializeBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(r, c);
            }
        }
    }

    private void placeMinesRandomly() {
        Random rand = new Random();
        int placed = 0;
        while (placed < mineCount) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (!grid[r][c].isMine()) {
                grid[r][c].setMine(true);
                placed++;
            }
        }
    }

    private void calculateAllAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!grid[r][c].isMine()) {
                    grid[r][c].setAdjacentMines(countAdjacentMines(r, c));
                }
            }
        }
    }

    private void placeSpecialCells() {
        Random rand = new Random();
        
        // הצבת שאלות
        int placedQ = 0;
        int attempts = 0;
        // מנסים למצוא מקום. הוספתי הגבלת ניסיונות למנוע לולאה אינסופית במקרים נדירים
        while (placedQ < questionCount && attempts < 1000) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            Cell cell = grid[r][c];
            
            // תנאי קריטי לפי ההוראות: רק משבצת ריקה (0 שכנים מוקשים) הופכת לשאלה
            if (!cell.isMine() && !cell.isQuestion() && !cell.isSurprise() && cell.getAdjacentMines() == 0) {
                cell.setQuestion(true);
                placedQ++;
            }
            attempts++;
        }
        
        // הצבת הפתעות
        int placedS = 0;
        attempts = 0;
        while (placedS < surpriseCount && attempts < 1000) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            Cell cell = grid[r][c];
            
            // תנאי קריטי לפי ההוראות: רק משבצת ריקה (0 שכנים מוקשים) הופכת להפתעה
            if (!cell.isMine() && !cell.isQuestion() && !cell.isSurprise() && cell.getAdjacentMines() == 0) {
                cell.setSurprise(true);
                placedS++;
            }
            attempts++;
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (inBounds(r, c) && !(r == row && c == col)) {
                    if (grid[r][c].isMine()) count++;
                }
            }
        }
        return count;
    }

    public void revealCell(int row, int col) {
        if (!inBounds(row, col)) return;
        Cell cell = grid[row][col];
        if (cell.isRevealed() || cell.isFlagged()) return;

        cell.reveal();

        // קסקדה: נמשכת רק אם המשבצת ריקה לגמרי (0 שכנים) ואיננה מיוחדת
        if (cell.getAdjacentMines() == 0 && !cell.isMine() && !cell.isQuestion() && !cell.isSurprise()) {
            cascadeReveal(row, col);
        }
    }

    private void cascadeReveal(int row, int col) {
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (inBounds(r, c) && !(r == row && c == col)) {
                    Cell neighbor = grid[r][c];
                    if (!neighbor.isRevealed() && !neighbor.isFlagged() && !neighbor.isMine()) {
                        neighbor.reveal();
                        // הרקורסיה ממשיכה רק אם גם השכן ריק לגמרי
                        if (neighbor.getAdjacentMines() == 0 && !neighbor.isQuestion() && !neighbor.isSurprise()) {
                            cascadeReveal(r, c);
                        }
                    }
                }
            }
        }
    }    
    public void ensureSafeStart(int r, int c) {
        // אם המשבצת שנבחרה היא לא מוקש - אין מה לעשות, הכל תקין
        if (!grid[r][c].isMine()) return;

        // 1. מסירים את המוקש מהמיקום שהשחקן לחץ עליו
        grid[r][c].setMine(false);

        // 2. מחפשים מיקום חדש אקראי למוקש
        Random rand = new Random();
        boolean placed = false;
        while (!placed) {
            int newR = rand.nextInt(rows);
            int newC = rand.nextInt(cols);
            
            // מוודאים שהמיקום החדש הוא לא המיקום המקורי ושאין שם כבר מוקש
            if (!(newR == r && newC == c) && !grid[newR][newC].isMine()) {
                grid[newR][newC].setMine(true);
                placed = true;
            }
        }

        // 3. קריטי: חישוב מחדש של כל המספרים בלוח (כי הזזנו מוקש)
        calculateAllAdjacentMines();
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public Cell getCell(int r, int c) { return grid[r][c]; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getMineCount() { return mineCount; }
    
    public void toggleFlag(int r, int c) {
        if (inBounds(r, c)) grid[r][c].toggleFlag(); 
    }
}