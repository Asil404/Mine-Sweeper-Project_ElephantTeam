package View;

import Model.Difficulty;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import Logic.GameFactory;
import Logic.AbstractGame;

public class LoginWindow extends JFrame {

    private JTextField p1NameField, p2NameField;
    private final String[] AVATARS = {"👤", "🤖", "👽", "🦊", "👾", "🤡", "👻", "🐱‍"};
    private int p1AvatarIndex = 0, p2AvatarIndex = 1;
    private JButton p1AvatarBtn, p2AvatarBtn;
    
    private Difficulty selectedDifficulty = Difficulty.EASY; 
    private JButton btnEasy, btnMedium, btnHard;
    private JLabel diffDetailsLabel;
    
    private List<Particle> particles = new ArrayList<>();
    private Timer animTimer;

    public LoginWindow() {
        setTitle("MineSweeper - Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setUndecorated(true);                    
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        initParticles();

        JPanel rootPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(44, 62, 80), 0, h, Color.BLACK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                drawGrid(g2, w, h);
                drawParticles(g2);
            }
        };
        setContentPane(rootPanel);
        
        animTimer = new Timer(30, e -> { updateParticles(); rootPanel.repaint(); });
        animTimer.start();

        // --- כפתור חזרה ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(30, 30, 0, 0));
        
        JButton backBtn = createRoundedButton("BACK", new Color(100, 100, 100));
        backBtn.setPreferredSize(new Dimension(120, 40));
        backBtn.addActionListener(e -> { animTimer.stop(); dispose(); new WelcomeWindow(); });
        
        topBar.add(backBtn);
        rootPanel.add(topBar, BorderLayout.NORTH);

        // פאנל מרכזי
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("PLAYER SETUP");
        title.setFont(new Font("Verdana", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(title, gbc);

        gbc.gridy++;
        // כאן אנחנו יוצרים את השדות עם ה-Placeholder
        mainPanel.add(createStyledInput("Player1", 1), gbc);
        gbc.gridy++;
        mainPanel.add(createStyledInput("Player2", 2), gbc);

        gbc.gridy++;
        JLabel diffLabel = new JLabel("SELECT DIFFICULTY");
        diffLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        diffLabel.setForeground(new Color(220, 220, 220));
        mainPanel.add(diffLabel, gbc);

        gbc.gridy++;
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        btnPanel.setOpaque(false);
        btnEasy = createDiffButton("EASY", new Color(46, 213, 115), Difficulty.EASY);
        btnMedium = createDiffButton("MEDIUM", new Color(255, 159, 67), Difficulty.MEDIUM);
        btnHard = createDiffButton("HARD", new Color(255, 107, 107), Difficulty.HARD);
        btnPanel.add(btnEasy); btnPanel.add(btnMedium); btnPanel.add(btnHard);
        mainPanel.add(btnPanel, gbc);

        gbc.gridy++;
        diffDetailsLabel = new JLabel();
        diffDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        diffDetailsLabel.setForeground(Color.WHITE);
        updateDifficultyVisuals();
        mainPanel.add(diffDetailsLabel, gbc);

        gbc.gridy++;
        JButton startButton = createRoundedButton("START MISSION", new Color(108, 92, 231));
        startButton.setPreferredSize(new Dimension(300, 60));
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 22));
        startButton.addActionListener(e -> startGame());
        mainPanel.add(startButton, gbc);

        gbc.gridy++;
        JButton helpBtn = new JButton("How to Play?");
        helpBtn.setForeground(new Color(200,200,200)); 
        helpBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        helpBtn.setContentAreaFilled(false); helpBtn.setBorderPainted(false); helpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpBtn.addActionListener(e -> new InstructionsWindow());
        mainPanel.add(helpBtn, gbc);

        rootPanel.add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }
    
    // --- השינוי הגדול: יצירת שדה עם Placeholder (צללית) ---
    private JPanel createStyledInput(String placeholder, int pNum) {
        JPanel p = new JPanel(new BorderLayout()); 
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(450, 70));
        
        JTextField tf = new JTextField(placeholder);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        tf.setOpaque(false);
        tf.setCaretColor(Color.WHITE);
        
        // הגדרת עיצוב התחלתי (Placeholder - אפור)
        tf.setForeground(new Color(150, 150, 150)); 
        
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,2,0, new Color(108, 92, 231)),
            BorderFactory.createEmptyBorder(5,5,5,5)));

        // --- הוספת מאזין (Listener) לטיפול בטקסט ---
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // אם הטקסט הוא הדיפולט, נקה אותו ושנה ללבן
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                // אם המשתמש לא כתב כלום, החזר את הדיפולט באפור
                if (tf.getText().trim().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(new Color(150, 150, 150));
                }
            }
        });
        
        JButton avBtn = new JButton(pNum==1 ? AVATARS[p1AvatarIndex] : AVATARS[p2AvatarIndex]);
        avBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42)); 
        avBtn.setForeground(Color.WHITE); 
        avBtn.setContentAreaFilled(false); avBtn.setBorderPainted(false);
        avBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avBtn.setToolTipText("Click to change avatar");
        
        avBtn.addActionListener(e -> {
            if(pNum==1) { p1AvatarIndex=(p1AvatarIndex+1)%AVATARS.length; avBtn.setText(AVATARS[p1AvatarIndex]); }
            else { p2AvatarIndex=(p2AvatarIndex+1)%AVATARS.length; avBtn.setText(AVATARS[p2AvatarIndex]); }
        });

        if(pNum==1) { p1NameField=tf; p1AvatarBtn=avBtn; } else { p2NameField=tf; p2AvatarBtn=avBtn; }
        
        p.add(tf, BorderLayout.CENTER); p.add(avBtn, BorderLayout.EAST);
        return p;
    }
    
    private JButton createRoundedButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),30,30));
                super.paintComponent(g2); g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void startGame() {
        animTimer.stop();
        
        // קריאת הטקסט מהשדות
        String n1 = p1NameField.getText();
        String n2 = p2NameField.getText();
        
        if(n1.trim().isEmpty() || n1.equals("Player 1")) n1 = "Player 1";
        if(n2.trim().isEmpty() || n2.equals("Player 2")) n2 = "Player 2";
        
        dispose(); // סגירת חלון הלוגין

        // --- כאן נכנסות התבניות (השינוי) ---
        
        // 1. יצירת המפעל
        GameFactory factory = new GameFactory();
        
        // 2. יצירת המשחק דרך המפעל (Factory Method)
        // אנחנו מעבירים לו את כל מה שהמשתמש בחר
        AbstractGame game = factory.createGame(selectedDifficulty, n1, n2, AVATARS[p1AvatarIndex], AVATARS[p2AvatarIndex]);
        
        // 3. הפעלת המשחק דרך התבנית (Template Method)
        game.play();
    }
    
    private JButton createDiffButton(String t, Color c, Difficulty d) {
        JButton b = new JButton(t);
        b.setPreferredSize(new Dimension(130, 45));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(new LineBorder(c, 2));

        // הגדרות צבע התחלתיות
        b.setForeground(c); 
        b.setBackground(c); // שומרים את הצבע לרגע שייבחר

        // תיקון קריטי: כפתור לא נבחר הוא שקוף לגמרי
        b.setFocusPainted(false);
        b.setOpaque(false);      
        b.setContentAreaFilled(false);

        b.addActionListener(e -> { 
            selectedDifficulty = d; 
            updateDifficultyVisuals(); 
        });
        return b;
    }
    
    private void updateDifficultyVisuals() {
        // 1. איפוס כל הכפתורים למצב "לא נבחר" (שקוף + טקסט צבעוני)
        resetButtonVisuals(btnEasy, new Color(46, 213, 115));
        resetButtonVisuals(btnMedium, new Color(255, 159, 67));
        resetButtonVisuals(btnHard, new Color(255, 107, 107));

        // 2. מציאת הכפתור הפעיל ושינוי למצב "נבחר" (מלא + טקסט שחור)
        JButton active = null;
        if (selectedDifficulty == Difficulty.EASY) active = btnEasy;
        else if (selectedDifficulty == Difficulty.MEDIUM) active = btnMedium;
        else if (selectedDifficulty == Difficulty.HARD) active = btnHard;

        if (active != null) {
            active.setOpaque(true);             // הופך לאטום כדי שיראו את הצבע רקע
            active.setContentAreaFilled(true);  // ממלא את הרקע
            active.setBackground(active.getForeground()); // לוקח את הצבע מהבורדר/טקסט ושם ברקע
            active.setForeground(Color.BLACK);  // טקסט שחור לקונטרסט
        }

        // עדכון לייבל למטה
        Difficulty d = selectedDifficulty;
        if (diffDetailsLabel != null) {
            diffDetailsLabel.setText(String.format("Grid: %dx%d | Mines: %d | Lives: %d", 
                d.getRows(), d.getCols(), d.getMines(), d.getLives()));
        }
    }

    // פונקציית עזר קטנה למניעת שכפול קוד
    private void resetButtonVisuals(JButton btn, Color originalColor) {
        btn.setOpaque(false);             
        btn.setContentAreaFilled(false);  
        btn.setForeground(originalColor);
        btn.setBackground(originalColor); // שומרים בזיכרון את הצבע (לא רואים אותו כי זה שקוף)
    }
    private void drawGrid(Graphics2D g2, int w, int h) {
    	g2.setColor(new Color(255,255,255,10)); for(int x=0;x<w;x+=50)
    		g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=50) g2.drawLine(0,y,w,y); 
    		}
    
    private void initParticles() {
    	Random r=new Random();
    	for(int i=0;i<30;i++) 
    		particles.add(new Particle(r.nextInt(1920), r.nextInt(1080))); 
    	}
    
    private void updateParticles() { 
    	for(Particle p:particles) {
    		p.y-=p.speed;
    		if(p.y<-20) p.y=getHeight();
    		}
    	}
    private void drawParticles(Graphics2D g2) {
    	g2.setColor(new Color(255,255,255,30));
    	for(Particle p:particles)
    		g2.fillOval(p.x,p.y,p.size,p.size); 
    	}
    private class Particle {
    	int x,y,size,speed; Particle(int x,int y){
    		this.x=x;
    		this.y=y;
    		size=new Random().nextInt(5)+3;speed=new Random().nextInt(3)+1;
    		} 
    	}
}