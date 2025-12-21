package View;

import Model.Question;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class QuestionPopup extends JDialog {

    private boolean isCorrect = false;
    private final Color BG_COLOR = new Color(44, 62, 80);
    private final Color BTN_COLOR = new Color(52, 152, 219);
    private final Color TEXT_COLOR = Color.WHITE;

    public QuestionPopup(JFrame parent, Question q) {
        super(parent, "Bonus Question!", true); // true = מודאלי (חוסם את החלון הראשי)
        setSize(500, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        
        // פאנל ראשי
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // הצגת השאלה
        JLabel qLabel = new JLabel("<html><center>" + q.getQuestionText() + "</center></html>");
        qLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        qLabel.setForeground(TEXT_COLOR);
        qLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(qLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // פאנל כפתורים לתשובות
        JPanel answersPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        answersPanel.setOpaque(false);

        String[] answers = q.getAnswers();
        for (int i = 0; i < 4; i++) {
            JButton btn = new JButton(answers[i]);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.setBackground(BTN_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            final int index = i;
            btn.addActionListener((ActionEvent e) -> {
                // ✅ התיקון: שימוש בשם הפונקציה הנכון (getCorrectAnsIndex)
                if (index == q.getCorrectAnsIndex()) {
                    isCorrect = true;
                    JOptionPane.showMessageDialog(this, "Correct Answer! 🎉", "Well Done", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    isCorrect = false;
                    // גם כאן צריך את השם החדש כדי להציג את התשובה הנכונה
                    String correctText = q.getAnswers()[q.getCorrectAnsIndex()];
                    JOptionPane.showMessageDialog(this, "Wrong Answer! 😞\nThe correct answer was:\n" + correctText, "Oops...", JOptionPane.ERROR_MESSAGE);
                }
                dispose(); // סגירת החלון
            });
            
            answersPanel.add(btn);
        }

        mainPanel.add(answersPanel);
        add(mainPanel, BorderLayout.CENTER);
    }

    public boolean isAnswerCorrect() {
        return isCorrect;
    }
}