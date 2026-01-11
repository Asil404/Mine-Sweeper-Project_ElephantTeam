package Logic;

public abstract class AbstractGame {
    
    // --- תבנית TEMPLATE METHOD ---
    // מגדירה את סדר הפעולות: אתחול -> יצירת חלון -> התחלה
    public final void play() {
        initializeGame();
        createWindow();
        startGame();
    }

    protected abstract void initializeGame();
    protected abstract void createWindow();
    protected abstract void startGame();
}