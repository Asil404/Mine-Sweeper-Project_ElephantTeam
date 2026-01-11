package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Added for the dialog
import java.awt.*;
import java.awt.event.MouseAdapter; // Added for dialog interaction
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer; 
public class WelcomeWindow extends JFrame {

    private final Color COLOR_BG_START = new Color(44, 62, 80);
    private final Color COLOR_BG_END = new Color(0, 0, 0);
    private final Color COLOR_BTN_START = new Color(108, 92, 231);
    private final Color COLOR_BTN_ADMIN = new Color(255, 118, 117);
    private final Color COLOR_BTN_EXIT = new Color(99, 110, 114);
    private final Color COLOR_BTN_HISTORY = new Color(100, 100, 100);

    private List<Particle> particles = new ArrayList<>();
    private Timer animTimer;

    public WelcomeWindow() {
        setTitle("MineSweeper - Welcome");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initParticles();

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth(), h = getHeight();

                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, h, COLOR_BG_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);

                drawGrid(g2d, w, h);
                drawParticles(g2d);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        animTimer = new Timer(30, e -> {
            updateParticles();
            mainPanel.repaint();
        });
        animTimer.start();

        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setOpaque(false);
        
        JLabel iconLabel = new JLabel("💣");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        iconLabel.setForeground(new Color(255, 118, 117));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); 

        JPanel iconWrapper = new JPanel(new GridBagLayout()) { 
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = 140;    
                
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                g2.setColor(new Color(255, 118, 117, 50)); 
                g2.fillOval(x, y, size, size);
                g2.setColor(new Color(255, 118, 117, 100)); 
                g2.drawOval(x, y, size, size);
            }
        };

        iconWrapper.setOpaque(false);
        iconWrapper.add(iconLabel);
        iconWrapper.setPreferredSize(new Dimension(200, 200)); 
        iconWrapper.setMaximumSize(new Dimension(200, 200));

        JLabel titleLabel = new JLabel("MINESWEEPER");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel subTitleLabel = new JLabel("PRO EDITION");
        subTitleLabel.setFont(new Font("Verdana", Font.PLAIN, 18));
        subTitleLabel.setForeground(new Color(162, 155, 254));
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        // --- Buttons ---
        
        JButton btnStart = createStyledButton("START GAME", COLOR_BTN_START);
        btnStart.addActionListener(e -> {
            animTimer.stop();
            dispose();
            new LoginWindow();
        });

        JButton historyBtn = createStyledButton("HISTORY", COLOR_BTN_HISTORY);
        historyBtn.addActionListener(e -> {
             new HistoryWindow(); 
        });

        JButton btnAdmin = createStyledButton("QUESTIONS MANAGEMENT", COLOR_BTN_ADMIN);
        btnAdmin.addActionListener(e -> {
            animTimer.stop();
            dispose();
            new AdminWindow();
        });

        // --- UPDATED EXIT BUTTON WITH CONFIRMATION ---
        JButton btnExit = createStyledButton("EXIT GAME", COLOR_BTN_EXIT);
        btnExit.addActionListener(e -> {
            // This calls the custom dialog we added at the bottom
            if (showCustomConfirm("Exit Game", "Are you sure you want to quit?")) {
                System.exit(0);
            }
        });

        contentBox.add(iconWrapper);
        contentBox.add(titleLabel);
        contentBox.add(subTitleLabel);
        
        contentBox.add(btnStart);
        contentBox.add(Box.createVerticalStrut(20));
        
        contentBox.add(historyBtn);
        contentBox.add(Box.createVerticalStrut(20));
        
        contentBox.add(btnAdmin);
        contentBox.add(Box.createVerticalStrut(20));
        
        contentBox.add(btnExit);

        add(contentBox);
        setVisible(true);
    }
    
   

    private void drawGrid(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(255, 255, 255, 10));
        int step = 50;
        for (int x = 0; x < w; x += step) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += step) {
            g2.drawLine(0, y, w, y);
        }
    }

    private void initParticles() {
        Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            particles.add(new Particle(rand.nextInt(1920), rand.nextInt(1080)));
        }
    }

    private void updateParticles() {
        for (Particle p : particles) {
            p.y -= p.speed;
            if (p.y < -50) {
                p.y = getHeight() + 50;
                p.x = new Random().nextInt(getWidth());
            }
        }
    }

    private void drawParticles(Graphics2D g2) {
        for (Particle p : particles) {
            g2.setColor(new Color(255, 255, 255, p.alpha));
            g2.fillOval(p.x, p.y, p.size, p.size);
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(p.x - 2, p.y + p.size/2, p.x + p.size + 2, p.y + p.size/2);
            g2.drawLine(p.x + p.size/2, p.y - 2, p.x + p.size/2, p.y + p.size + 2);
        }
    }

    private class Particle {
        int x, y;
        int size;
        int speed;
        int alpha;

        public Particle(int x, int y) {
            this.x = x;
            this.y = y;
            Random r = new Random();
            this.size = r.nextInt(10) + 5;
            this.speed = r.nextInt(3) + 1;
            this.alpha = r.nextInt(50) + 20;
        }
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) g2.setColor(baseColor.brighter());
                else g2.setColor(baseColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 40, 40));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(350, 60));
        btn.setMaximumSize(new Dimension(350, 60));
        return btn;
    }

    private boolean showCustomConfirm(String title, String message) {
        JDialog dialog = new JDialog(this, title, true); // Modal
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
                
                // Red Border for Exit Warning
                g2.setColor(new Color(255, 100, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
            }
        };
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel("🚪");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel msgLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        msgLabel.setForeground(Color.WHITE);

        // Buttons for the Dialog
        JButton yesBtn = createStyledButton("YES, EXIT", new Color(231, 76, 60)); // Red
        yesBtn.setPreferredSize(new Dimension(150, 40));
        yesBtn.setMaximumSize(new Dimension(150, 40));
        yesBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        JButton noBtn = createStyledButton("CANCEL", new Color(149, 165, 166)); // Grey
        noBtn.setPreferredSize(new Dimension(150, 40));
        noBtn.setMaximumSize(new Dimension(150, 40));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WelcomeWindow();
        });
    }
}