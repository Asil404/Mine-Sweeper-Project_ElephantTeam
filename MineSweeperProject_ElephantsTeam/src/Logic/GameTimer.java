package Logic;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class GameTimer {
    private Timer timer;
    private int seconds = 0;
    private List<GameObserver> observers = new ArrayList<>(); // רשימת המאזינים

    public GameTimer() {
        // טיימר שרץ כל 1000 מילישניות (שנייה אחת)
        timer = new Timer(1000, e -> {
            seconds++;
            notifyObservers();
        });
    }

    // הרשמה לעדכונים (Observer Pattern)
    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
    
    public int getTime() {
        return seconds;
    }

    // הודעה לכל המאזינים שהזמן השתנה
    private void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onTimeUpdate(seconds);
        }
    }
}