package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FloatingLabel extends JLabel {
    private int alpha = 255; // שקיפות התחלתית (מלאה)
    private int yOffset = 0; // כמה למעלה הוא זז
    private Timer animTimer;

    public FloatingLabel(String text, int startX, int startY, Color color, JLayeredPane parent) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 20));
        setForeground(color);
        setBounds(startX, startY, 100, 30); // גודל הטקסט
        
        parent.add(this, JLayeredPane.POPUP_LAYER); // מוסיף לשכבה העליונה

        // טיימר לאנימציה (רץ כל 30 מילישניות)
        animTimer = new Timer(30, (ActionEvent e) -> {
            yOffset -= 2; // זז למעלה
            alpha -= 10;  // נעלם לאט לאט

            if (alpha <= 0) {
                alpha = 0;
                animTimer.stop();
                parent.remove(this); // מוחק את עצמו כשהופך לשקוף
                parent.repaint();
            } else {
                setLocation(startX, startY + yOffset);
                setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            }
        });
        animTimer.start();
    }
}