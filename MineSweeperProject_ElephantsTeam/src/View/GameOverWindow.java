package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GameOverWindow extends JFrame {

    public GameOverWindow(int finalScore, boolean isWin) {
        setTitle("Game Over");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(isWin ? new Color(39, 174, 96) : new Color(192, 57, 43));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10,0,10,0);

        JLabel icon = new JLabel(isWin ? "🏆" : "☠️");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
        mainPanel.add(icon, gbc);

        gbc.gridy++;
        JLabel title = new JLabel(isWin ? "VICTORY!" : "GAME OVER");
        title.setFont(new Font("Verdana", Font.BOLD, 60));
        title.setForeground(Color.WHITE);
        mainPanel.add(title, gbc);

        gbc.gridy++;
        JLabel scoreLabel = new JLabel("Final Score: " + finalScore);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        scoreLabel.setForeground(new Color(255,255,255,200));
        mainPanel.add(scoreLabel, gbc);

        gbc.gridy++;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        
        JButton homeBtn = createBtn("MAIN MENU");
        homeBtn.addActionListener(e -> { dispose(); new WelcomeWindow(); });
        
        JButton exitBtn = createBtn("EXIT GAME");
        exitBtn.addActionListener(e -> System.exit(0));
        
        btnPanel.add(homeBtn);
        btnPanel.add(exitBtn);
        mainPanel.add(btnPanel, gbc);

        add(mainPanel);
        setVisible(true);
    }

    private JButton createBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 18));
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setPreferredSize(new Dimension(200, 60));
        b.setFocusPainted(false);
        return b;
    }
}