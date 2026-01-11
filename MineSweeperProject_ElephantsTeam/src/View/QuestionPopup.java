package View;

import Model.Question;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;

public class QuestionPopup extends JDialog {

    private boolean isCorrect = false;
    // צבעים
    private final Color BG_COLOR_START = new Color(44, 62, 80);
    private final Color BG_COLOR_END = new Color(30, 40, 50);
    private final Color BTN_COLOR = new Color(52, 152, 219);
    
    private JPanel contentPanel;
    private Question currentQuestion;

    public QuestionPopup(JFrame parent, Question q) {
        super(parent, "Bonus Question", true);
        this.currentQuestion = q;
        
        // הקטנתי טיפה את הגובה כדי שהכל ייראה הדוק יותר
        setSize(500, 380);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));

        // פאנל רקע ראשי
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_COLOR_START, 0, getHeight(), BG_COLOR_END);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                
                g2.setColor(new Color(255,255,255,40));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 30, 30));
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); // קצת יותר מרווח בצדדים
        
        setContentPane(contentPanel);
        initQuestionUI();
    }

    // --- מסך 1: השאלה ---
    private void initQuestionUI() {
        contentPanel.removeAll();
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel title = new JLabel("BONUS QUESTION");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(52, 152, 219));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(title);
        
        centerPanel.add(Box.createVerticalStrut(15));

        JLabel qLabel = new JLabel("<html><center>" + currentQuestion.getQuestionText() + "</center></html>");
        qLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        qLabel.setForeground(Color.WHITE);
        qLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(qLabel);
        
        centerPanel.add(Box.createVerticalStrut(25));

        JPanel answersPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        answersPanel.setOpaque(false);

        String[] answers = currentQuestion.getAnswers();
        for (int i = 0; i < 4; i++) {
            JButton btn = new JButton(answers[i]);
            styleButton(btn, BTN_COLOR);
            final int index = i;
            btn.addActionListener((ActionEvent e) -> {
                if (index == currentQuestion.getCorrectAnsIndex()) {
                    isCorrect = true;
                    showFeedbackUI(true);
                } else {
                    isCorrect = false;
                    showFeedbackUI(false);
                }
            });
            answersPanel.add(btn);
        }

        centerPanel.add(answersPanel);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // --- מסך 2: הפידבק (התיקון הגדול) ---
    private void showFeedbackUI(boolean success) {
        contentPanel.removeAll();
        
        // שימוש ב-GridBagLayout כדי למרכז הכל יפה באמצע
        JPanel feedbackPanel = new JPanel(new GridBagLayout());
        feedbackPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 0, 15, 0); // רווחים בין האלמנטים

        Color accentColor = success ? new Color(46, 213, 115) : new Color(255, 71, 87);

        // 1. אייקון (הגדלתי אותו ל-110 כדי שייראה טוב יותר)
        StatusIconPanel icon = new StatusIconPanel(success, accentColor);
        feedbackPanel.add(icon, gbc);
        
        // 2. כותרת
        gbc.gridy++;
        JLabel title = new JLabel(success ? "CORRECT!" : "WRONG!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36)); // פונט גדול יותר
        title.setForeground(accentColor);
        feedbackPanel.add(title, gbc);

        // 3. הצגת התשובה הנכונה (רק אם טעינו)
        if (!success) {
            gbc.gridy++;
            String correctText = currentQuestion.getAnswers()[currentQuestion.getCorrectAnsIndex()];
            JLabel correctLbl = new JLabel("<html><center><span style='font-size:14px; color:#bdc3c7;'>The correct answer was:</span><br><span style='font-size:18px; color:#f1c40f;'><b>" + correctText + "</b></span></center></html>");
            feedbackPanel.add(correctLbl, gbc);
        } else {
            // רווח קטן אם צדקנו כדי לאזן
            gbc.gridy++;
            feedbackPanel.add(Box.createVerticalStrut(20), gbc);
        }

        // 4. כפתור
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0); // קצת רווח לפני הכפתור
        JButton continueBtn = new JButton("CONTINUE");
        styleButton(continueBtn, accentColor);
        // הקטנתי טיפה את הכפתור שייראה פרופורציונלי
        continueBtn.setPreferredSize(new Dimension(160, 45)); 
        continueBtn.addActionListener(e -> dispose());
        feedbackPanel.add(continueBtn, gbc);

        contentPanel.add(feedbackPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public boolean isAnswerCorrect() {
        return isCorrect;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (btn.getModel().isRollover()) g2.setColor(color.brighter());
                else g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 20, 20));
                super.paint(g2, c);
                g2.dispose();
            }
        });
    }

    // --- מחלקה לציור האייקון ---
    private class StatusIconPanel extends JPanel {
        private boolean isCorrect;
        private Color color;

        public StatusIconPanel(boolean isCorrect, Color color) {
            this.isCorrect = isCorrect;
            this.color = color;
            setOpaque(false);
            // הגדלתי טיפה את האייקון
            setPreferredSize(new Dimension(110, 110)); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int size = Math.min(getWidth(), getHeight());
            int padding = 10;
            
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
            g2.fillOval(padding, padding, size - 2*padding, size - 2*padding);
            g2.setColor(color);
            g2.drawOval(padding, padding, size - 2*padding, size - 2*padding);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int iconSize = size / 4; 

            if (isCorrect) {
                Path2D check = new Path2D.Float();
                check.moveTo(centerX - iconSize, centerY);
                check.lineTo(centerX - iconSize/3, centerY + iconSize);
                check.lineTo(centerX + iconSize, centerY - iconSize);
                g2.draw(check);
            } else {
                g2.drawLine(centerX - iconSize, centerY - iconSize, centerX + iconSize, centerY + iconSize);
                g2.drawLine(centerX + iconSize, centerY - iconSize, centerX - iconSize, centerY + iconSize);
            }
        }
    }
}