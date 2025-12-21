package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WelcomeWindow extends JFrame {

    private final Color COLOR_BG_START = new Color(44, 62, 80);
    private final Color COLOR_BG_END = new Color(0, 0, 0);
    private final Color COLOR_BTN_START = new Color(108, 92, 231);
    private final Color COLOR_BTN_ADMIN = new Color(255, 118, 117);
    private final Color COLOR_BTN_EXIT = new Color(99, 110, 114);

    // רשימת חלקיקים לאנימציה ברקע
    private List<Particle> particles = new ArrayList<>();
    private Timer animTimer;

    public WelcomeWindow() {
        setTitle("MineSweeper - Welcome");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // מסך מלא
        setUndecorated(true);                    // ללא מסגרת
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // אתחול החלקיקים
        initParticles();

        // פאנל ראשי עם ציור מיוחד (Gradient + Grid + Particles)
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int w = getWidth(), h = getHeight();

                // 1. רקע Gradient
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, h, COLOR_BG_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);

                // 2. רשת (Grid) עדינה ברקע
                drawGrid(g2d, w, h);

                // 3. ציור החלקיקים
                drawParticles(g2d);
            }
        };
        mainPanel.setLayout(new GridBagLayout()); // שימוש ב-GridBag למרכוז מושלם
        setContentPane(mainPanel);

        // --- התחלת הטיימר לאנימציה ---
        animTimer = new Timer(30, e -> {
            updateParticles();
            mainPanel.repaint();
        });
        animTimer.start();

        // --- בניית התוכן המרכזי ---
        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setOpaque(false); // שקוף כדי לראות את הרקע
        
        // אייקון פצצה משודרג (עם הילה)
        JLabel iconLabel = new JLabel("💣");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        iconLabel.setForeground(new Color(255, 118, 117)); // צבע אדום לפצצה
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // יצירת פאנל עוטף לאייקון כדי לצייר לו הילה זוהרת
        JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            @Override
            protected void paintComponent(Graphics g) {
                // ציור הילה מאחורי הפצצה
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 120;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // עיגול זוהר חצי שקוף
                g2.setColor(new Color(255, 118, 117, 50)); 
                g2.fillOval(x, y, size, size);
                g2.setColor(new Color(255, 118, 117, 100)); 
                g2.drawOval(x, y, size, size);
            }
        };
        iconWrapper.setOpaque(false);
        iconWrapper.add(iconLabel);
        iconWrapper.setMaximumSize(new Dimension(200, 150));

        // כותרות
        JLabel titleLabel = new JLabel("MINESWEEPER");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // אפקט צל לכותרת
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel subTitleLabel = new JLabel("PRO EDITION");
        subTitleLabel.setFont(new Font("Verdana", Font.PLAIN, 18));
        subTitleLabel.setForeground(new Color(162, 155, 254)); // סגול בהיר
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        // כפתורים
        JButton btnStart = createStyledButton("START GAME", COLOR_BTN_START);
        btnStart.addActionListener(e -> {
            animTimer.stop();
            dispose();
            new LoginWindow();
        });

        JButton btnAdmin = createStyledButton("QUESTIONS MANAGEMENT", COLOR_BTN_ADMIN);
        btnAdmin.addActionListener(e -> {
            animTimer.stop();
            dispose();
            new AdminWindow();
        });

        JButton btnExit = createStyledButton("EXIT GAME", COLOR_BTN_EXIT);
        btnExit.addActionListener(e -> System.exit(0));

        // הוספה לפאנל התוכן
        contentBox.add(iconWrapper);
        contentBox.add(titleLabel);
        contentBox.add(subTitleLabel);
        contentBox.add(btnStart);
        contentBox.add(Box.createVerticalStrut(20));
        contentBox.add(btnAdmin);
        contentBox.add(Box.createVerticalStrut(20));
        contentBox.add(btnExit);

        add(contentBox);
        setVisible(true);
    }
    
    // --- לוגיקת האנימציה והרקע ---

    private void drawGrid(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(255, 255, 255, 10)); // לבן חלש מאוד
        int step = 50; // גודל המשבצות ברקע
        
        // קווים אנכיים
        for (int x = 0; x < w; x += step) {
            g2.drawLine(x, 0, x, h);
        }
        // קווים אופקיים
        for (int y = 0; y < h; y += step) {
            g2.drawLine(0, y, w, y);
        }
    }

    private void initParticles() {
        Random rand = new Random();
        for (int i = 0; i < 30; i++) { // כמות החלקיקים
            particles.add(new Particle(rand.nextInt(1920), rand.nextInt(1080)));
        }
    }

    private void updateParticles() {
        for (Particle p : particles) {
            p.y -= p.speed; // תנועה למעלה
            if (p.y < -50) { // אם יצא מהמסך, תחזור למטה
                p.y = getHeight() + 50;
                p.x = new Random().nextInt(getWidth());
            }
        }
    }

    private void drawParticles(Graphics2D g2) {
        for (Particle p : particles) {
            g2.setColor(new Color(255, 255, 255, p.alpha));
            // ציור צורה של מוקש קטן (עיגול עם צלב)
            g2.fillOval(p.x, p.y, p.size, p.size);
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(p.x - 2, p.y + p.size/2, p.x + p.size + 2, p.y + p.size/2);
            g2.drawLine(p.x + p.size/2, p.y - 2, p.x + p.size/2, p.y + p.size + 2);
        }
    }

    // מחלקה פנימית לייצוג חלקיק
    private class Particle {
        int x, y;
        int size;
        int speed;
        int alpha; // שקיפות

        public Particle(int x, int y) {
            this.x = x;
            this.y = y;
            Random r = new Random();
            this.size = r.nextInt(10) + 5; // גודל רנדומלי
            this.speed = r.nextInt(3) + 1; // מהירות רנדומלית
            this.alpha = r.nextInt(50) + 20; // שקיפות רנדומלית
        }
    }

    // --- עיצוב כפתורים ---
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
        btn.setPreferredSize(new Dimension(350, 60)); // כפתורים גדולים יותר
        btn.setMaximumSize(new Dimension(350, 60));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WelcomeWindow::new);
    }
}