package View;

import Model.Difficulty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GameOverWindow extends JFrame {

    private final Color COLOR_BG_START = new Color(44, 62, 80);
    private final Color COLOR_BG_END = new Color(0, 0, 0);
    
    private Difficulty diff;
    private String p1, p2, av1, av2;

    public GameOverWindow(int score, boolean isVictory, Difficulty diff, String p1, String p2, String av1, String av2) {
        this.diff = diff;
        this.p1 = p1; this.p2 = p2;
        this.av1 = av1; this.av2 = av2;

        setTitle("Game Over");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(mainPanel);

        // --- תוכן מרכזי (GridBagLayout למרכוז) ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // כותרת (עם פונט אימוג'י)
        JLabel titleLbl = new JLabel(isVictory ? "VICTORY! 🏆" : "GAME OVER 💀");
        titleLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 80)); 
        titleLbl.setForeground(isVictory ? new Color(46, 204, 113) : new Color(231, 76, 60));
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);

        // ניקוד
        JLabel scoreLbl = new JLabel("Final Score: " + score);
        scoreLbl.setFont(new Font("Segoe UI", Font.BOLD, 40));
        scoreLbl.setForeground(Color.WHITE);
        scoreLbl.setHorizontalAlignment(SwingConstants.CENTER);

        centerPanel.add(titleLbl, gbc);
        gbc.gridy = 1;
        centerPanel.add(scoreLbl, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // --- כפתורים למטה ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 60));
        btnPanel.setOpaque(false);

        JButton menuBtn = createStyledButton("MAIN MENU 🏠", new Color(149, 165, 166));
        menuBtn.addActionListener(e -> {
            dispose();
            new WelcomeWindow();
        });

        JButton rematchBtn = createStyledButton("REMATCH 🔄", new Color(52, 152, 219));
        rematchBtn.addActionListener(e -> {
            dispose();
            new GameWindow(diff, p1, p2, av1, av2);
        });

        JButton exitBtn = createStyledButton("EXIT ❌", new Color(231, 76, 60));
        exitBtn.addActionListener(e -> {
            if (showCustomConfirm("Exit Application", "Are you sure you want to close the game completely?")) {
                System.exit(0);
            }
        });

        btnPanel.add(menuBtn);
        btnPanel.add(rematchBtn);
        btnPanel.add(exitBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16)); 
        // ----------------------------------------------
        
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 50));
        return btn;
    }

    private boolean showCustomConfirm(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(450, 220);
        dialog.setLocationRelativeTo(this);
        
        final boolean[] result = {false};

        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 100, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
            }
        };
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // גם כאן חשוב פונט אימוג'י לאייקון הדלת
        JLabel icon = new JLabel("🚪");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50)); 
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel msgLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        msgLabel.setForeground(Color.WHITE);

        JButton yesBtn = createStyledButton("YES, EXIT", new Color(231, 76, 60));
        yesBtn.setPreferredSize(new Dimension(150, 40));
        yesBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        JButton noBtn = createStyledButton("CANCEL", new Color(149, 165, 166));
        noBtn.setPreferredSize(new Dimension(150, 40));
        noBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(yesBtn);
        btnPanel.add(noBtn);

        content.add(icon, BorderLayout.NORTH);
        content.add(msgLabel, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
        
        return result[0];
    }
}