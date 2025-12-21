package View;

import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Difficulty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage; 
import java.util.Random;

public class GameWindow extends JFrame {

    private GameController controller;
    private ModernButton[][] buttons1;
    private ModernButton[][] buttons2;
    
    // --- תוויות נפרדות לכל רכיב ---
    private JLabel lblTurn;
    private JLabel lblTimer;
    private JLabel lblScore;
    private JLabel lblLives;
    
    private Difficulty currentDiff;
    private String p1Name, p2Name;
    private String p1Avatar, p2Avatar;
    
    private Timer gameTimer;
    private int secondsPlayed = 0;
    private boolean devMode = false;
    private JButton pauseBtn, homeBtn, themeBtn, restartBtn;
    private JProgressBar progressBar;
    private JPanel boardsContainer; 
    private CardLayout cardLayout;  

    private boolean isDarkMode = true; 
    private Color colorBgStart, colorBgEnd, colorText, colorRevealedBg;

    public GameWindow(Difficulty difficulty, String p1Name, String p2Name, String p1Avatar, String p2Avatar) {
        this.currentDiff = difficulty;
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.p1Avatar = p1Avatar;
        this.p2Avatar = p2Avatar;

        this.controller = new GameController(difficulty, p1Name, p2Name);
        this.controller.setViewFrame(this);

        setTitle("MineSweeper - Pro Edition");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // מסך מלא
        setUndecorated(true);                    // ללא מסגרת
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        applyThemeColors();
        
        setContentPane(new BackgroundPanel());
        setLayout(new BorderLayout());

        setCustomCursor(); 

        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_D) toggleDevMode();
                if (e.getKeyCode() == KeyEvent.VK_P) togglePauseGame(); 
                if (e.getKeyCode() == KeyEvent.VK_R) restartGame(); 
            }
        });

        // =================================================================
        // 1. פאנל עליון (Top HUD)
        // =================================================================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- צד שמאל: כפתורים + תור ---
        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftContainer.setOpaque(false);
        
        // כפתורים
        homeBtn = new JButton("🏠"); styleControlButton(homeBtn); homeBtn.addActionListener(e -> returnToHome());
        restartBtn = new JButton("🔄"); styleControlButton(restartBtn); restartBtn.addActionListener(e -> restartGame());
        pauseBtn = new JButton("⏸"); styleControlButton(pauseBtn); pauseBtn.addActionListener(e -> togglePauseGame());
        themeBtn = new JButton("🌗"); styleControlButton(themeBtn); themeBtn.addActionListener(e -> toggleTheme());
        
        leftContainer.add(homeBtn);
        leftContainer.add(restartBtn);
        leftContainer.add(pauseBtn);
        leftContainer.add(themeBtn);
        
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 30));
        sep.setForeground(new Color(255,255,255,50));
        leftContainer.add(sep);
        
        // שם השחקן
        lblTurn = new JLabel();
        lblTurn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        leftContainer.add(lblTurn);
        
        topPanel.add(leftContainer, BorderLayout.WEST);

        // --- אמצע: טיימר ---
        JPanel centerContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerContainer.setOpaque(false);
        lblTimer = new JLabel("00:00");
        lblTimer.setFont(new Font("Consolas", Font.BOLD, 36)); 
        lblTimer.setForeground(new Color(85, 239, 196)); 
        centerContainer.add(lblTimer);
        topPanel.add(centerContainer, BorderLayout.CENTER);

        // --- ימין: ניקוד ---
        JPanel rightContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightContainer.setOpaque(false);
        lblScore = new JLabel("Score: 0");
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblScore.setForeground(new Color(253, 203, 110)); 
        rightContainer.add(lblScore);
        topPanel.add(rightContainer, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // =================================================================
        // 2. פאנל מרכזי: הלוחות
        // =================================================================
        boardsContainer = new JPanel();
        cardLayout = new CardLayout();
        boardsContainer.setLayout(cardLayout);
        boardsContainer.setOpaque(false);
        
        JPanel gameGridPanel = new JPanel(new GridBagLayout());
        gameGridPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 40, 0, 40); 
        gbc.gridy = 0;

        // לוח 1
        gbc.gridx = 0;
        gameGridPanel.add(createBoardPanel(controller.getBoard1(), 1, p1Name, p1Avatar), gbc);

        // לוח 2
        gbc.gridx = 1;
        gameGridPanel.add(createBoardPanel(controller.getBoard2(), 2, p2Name, p2Avatar), gbc);
        
        // מסך השהייה
        JPanel pausedPanel = new JPanel(new GridBagLayout());
        pausedPanel.setOpaque(false);
        JLabel pauseLabel = new JLabel("<html><center><h1>GAME PAUSED</h1><br><font size='7'>🔒</font></center></html>");
        pauseLabel.setForeground(colorText); 
        pausedPanel.add(pauseLabel);

        boardsContainer.add(gameGridPanel, "GAME");
        boardsContainer.add(pausedPanel, "PAUSED");
        add(boardsContainer, BorderLayout.CENTER);

        // =================================================================
        // 3. פאנל תחתון (Bottom HUD): חיים
        // =================================================================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setForeground(new Color(46, 213, 115)); 
        progressBar.setBackground(new Color(255, 255, 255, 20));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 6)); 
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        
        JPanel heartsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        heartsPanel.setOpaque(false);
        heartsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        lblLives = new JLabel();
        lblLives.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32)); 
        lblLives.setForeground(new Color(255, 71, 87)); 
        heartsPanel.add(lblLives);
        
        bottomPanel.add(heartsPanel, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);

        updateHUD();
        setVisible(true);
        startTimer();
    }
    
    // --- עדכון הממשק ---
    private void updateHUD() {
        String currentAvatar = controller.isPlayer1Turn() ? p1Avatar : p2Avatar;
        String currentPlayer = controller.getCurrentPlayerName();
        String colorHex = controller.isPlayer1Turn() ? "#a29bfe" : "#ff7675"; 
        
        lblTurn.setText("<html><span style='font-family: Segoe UI Emoji; font-size: 24px; color: white;'>" + currentAvatar + "</span> " 
                        + "<span style='font-family: Segoe UI; font-size: 20px; color: " + colorHex + ";'>" + currentPlayer.toUpperCase() + "</span></html>");

        lblTimer.setText(String.format("%02d:%02d", secondsPlayed / 60, secondsPlayed % 60));
        lblScore.setText("SCORE: " + controller.getScore());
        lblLives.setText(getHeartsString(controller.getLives()));
        
        if (progressBar != null) progressBar.setValue(controller.getProgress()); 
    }

    private String getHeartsString(int lives) {
        StringBuilder sb = new StringBuilder();
        int displayLives = Math.min(lives, 10);
        for (int i = 0; i < displayLives; i++) sb.append("❤ "); 
        if (lives > 10) sb.append("+"); 
        return sb.toString();
    }
    
    // --- יצירת לוח ---
    private JPanel createBoardPanel(Board board, int playerID, String playerName, String avatar) {
        int rows = board.getRows();
        int cols = board.getCols();
        
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        
        Color playerThemeColor = (playerID == 1) ? new Color(108, 92, 231) : new Color(255, 118, 117);
        
        JLabel label = new JLabel("<html><span style='font-family: Segoe UI Emoji; font-size: 24px;'>" + avatar + "</span> " 
                                + "<span style='font-family: Segoe UI; font-size: 20px;'>" + playerName.toUpperCase() + "</span></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(playerThemeColor); 
        panel.add(label, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
        gridPanel.setOpaque(false); 
        
        ModernButton[][] buttonArray = new ModernButton[rows][cols];
        if (playerID == 1) buttons1 = buttonArray; else buttons2 = buttonArray;

        int buttonSize;
        int fontSize;
        if (currentDiff == Difficulty.HARD) { buttonSize = 32; fontSize = 18; }
        else if (currentDiff == Difficulty.MEDIUM) { buttonSize = 42; fontSize = 24; }
        else { buttonSize = 52; fontSize = 30; }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                ModernButton button = new ModernButton(playerThemeColor, fontSize);
                button.setPreferredSize(new Dimension(buttonSize, buttonSize)); 
                
                final int row = r; final int col = c;

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (controller.isPaused()) return; 
                        boolean isP1Turn = controller.isPlayer1Turn();
                        boolean isMyTurn = (playerID == 1 && isP1Turn) || (playerID == 2 && !isP1Turn);
                        if (!isMyTurn) { triggerShakeEffect(); return; }

                        int oldScore = controller.getScore(); 
                        
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            if (button.isFlagged) { triggerShakeEffect(); return; }
                            controller.handleLeftClick(row, col, playerID);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            controller.handleRightClick(row, col, playerID);
                        }
                        
                        refreshView();
                        int scoreDiff = controller.getScore() - oldScore;
                        if (scoreDiff != 0) spawnFloatingText(button, scoreDiff);

                        if (controller.isGameOver()) {
                            Timer t = new Timer(1000, evt -> showGameOverDialog());
                            t.setRepeats(false); t.start();
                        }
                    }
                });
                buttonArray[r][c] = button;
                gridPanel.add(button);
            }
        }
        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void refreshView() {
        updateHUD(); 
        updateBoardGrid(controller.getBoard1(), buttons1);
        updateBoardGrid(controller.getBoard2(), buttons2);
        repaint(); 
    }
    
    private void updateBoardGrid(Board board, ModernButton[][] buttons) {
        int rows = board.getRows(); int cols = board.getCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                ModernButton button = buttons[r][c];
                button.setUsed(cell.isUsed()); 
                if (cell.isRevealed()) {
                    button.setRevealed(true);
                    if (cell.isMine()) button.setSymbol("💣", new Color(231, 76, 60)); 
                    else if (cell.isQuestionWrong()) button.setSymbol("❌", Color.RED);
                    else if (cell.isQuestion()) button.setSymbol("?", new Color(241, 196, 15)); 
                    else if (cell.isSurprise()) button.setSymbol("🎁", new Color(155, 89, 182)); 
                    else {
                        int adj = cell.getAdjacentMines();
                        button.setSymbol(adj > 0 ? String.valueOf(adj) : "", getColorForNumber(adj));
                    }
                } else if (cell.isFlagged()) {
                    button.setRevealed(false); button.setFlagged(true);
                } else {
                    button.setRevealed(false); button.setFlagged(false); button.setSymbol("", colorText);
                }
                
                if (devMode && cell.isMine() && !cell.isRevealed()) button.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                else button.setBorder(null);
                button.repaint(); 
            }
        }
    }
    
    private Color getColorForNumber(int num) {
        switch (num) {
            case 1: return new Color(129, 236, 236);
            case 2: return new Color(85, 239, 196);
            case 3: return new Color(255, 118, 117);
            case 4: return new Color(162, 155, 254);
            case 5: return new Color(253, 203, 110);
            case 6: return new Color(225, 112, 85);
            default: return Color.WHITE;
        }
    }

    public void triggerShakeEffect() {
        final Point originalLocation = getLocation();
        final Random r = new Random();
        final Timer shakeTimer = new Timer(20, null);
        shakeTimer.addActionListener(new ActionListener() {
            int count = 0;
            public void actionPerformed(ActionEvent e) {
                int xOffset = r.nextInt(16) - 8; int yOffset = r.nextInt(16) - 8;
                setLocation(originalLocation.x + xOffset, originalLocation.y + yOffset);
                count++;
                if (count >= 10) { shakeTimer.stop(); setLocation(originalLocation); }
            }
        });
        shakeTimer.start();
    }
    
    private void spawnFloatingText(Component btn, int scoreDiff) {
        String text = (scoreDiff > 0) ? "+" + scoreDiff : String.valueOf(scoreDiff);
        Color color = (scoreDiff > 0) ? new Color(46, 213, 115) : new Color(255, 71, 87);
        Point p = SwingUtilities.convertPoint(btn, 0, 0, getLayeredPane());
        new FloatingLabel(text, p.x + 10, p.y - 10, color, getLayeredPane());
    }

    // --- ריסטארט מעודכן עם CustomDialog ---
    private void restartGame() {
        if (gameTimer != null) gameTimer.stop();
        
        int choice = CustomDialog.showConfirm(this, "Restart Game", "Are you sure you want to restart?<br>Current progress will be lost.");
        
        if (choice == CustomDialog.YES_OPTION) {
            this.dispose();
            new GameWindow(currentDiff, p1Name, p2Name, p1Avatar, p2Avatar);
        } else {
            if (!controller.isPaused()) gameTimer.start();
        }
    }

    // --- חזרה לבית מעודכנת עם CustomDialog ---
    private void returnToHome() {
        boolean wasPaused = controller.isPaused();
        if (!wasPaused) gameTimer.stop(); 
        
        int choice = CustomDialog.showConfirm(this, "Quit Game", "Return to Main Menu?<br>Your game will not be saved.");
        
        if (choice == CustomDialog.YES_OPTION) { 
            this.dispose(); 
            new WelcomeWindow(); 
        } else { 
            if (!wasPaused) gameTimer.start(); 
        }
    }

    private void togglePauseGame() {
        controller.togglePause();
        if (controller.isPaused()) {
            gameTimer.stop(); cardLayout.show(boardsContainer, "PAUSED"); 
            pauseBtn.setText("▶"); pauseBtn.setForeground(new Color(46, 213, 115));
        } else {
            gameTimer.start(); cardLayout.show(boardsContainer, "GAME"); 
            pauseBtn.setText("⏸"); pauseBtn.setForeground(colorText);
        }
    }

    private void startTimer() {
        gameTimer = new Timer(1000, e -> { secondsPlayed++; updateHUD(); });
        gameTimer.start();
    }
    public void updateStats() {
        updateHUD();
    }
    
    private void showGameOverDialog() {
        if (gameTimer != null) gameTimer.stop();
        this.dispose(); 
        new GameOverWindow(controller.getScore(), controller.isVictory());
    }
    
    private void toggleDevMode() { devMode = !devMode; refreshView(); }

    private void applyThemeColors() {
        if (isDarkMode) {
            colorBgStart = new Color(44, 62, 80); colorBgEnd = new Color(0, 0, 0);
            colorText = Color.WHITE; colorRevealedBg = new Color(255, 255, 255, 50); 
        } else {
            colorBgStart = new Color(223, 230, 233); colorBgEnd = new Color(255, 255, 255);
            colorText = new Color(45, 52, 54); colorRevealedBg = new Color(0, 0, 0, 50); 
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode; applyThemeColors();
        lblScore.setForeground(new Color(253, 203, 110)); 
        lblTimer.setForeground(new Color(85, 239, 196)); 
        homeBtn.setForeground(colorText); pauseBtn.setForeground(colorText);
        themeBtn.setForeground(colorText); restartBtn.setForeground(colorText);
        repaint();
    }

    private void styleControlButton(JButton btn) {
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        btn.setForeground(colorText);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(new Color(46, 213, 115)); } 
            public void mouseExited(MouseEvent e) { btn.setForeground(colorText); } 
        });
    }

    private void setCustomCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(new Color(0, 255, 255)); g2.setStroke(new BasicStroke(2));
        g2.draw(new Line2D.Float(16, 0, 16, 32)); g2.draw(new Line2D.Float(0, 16, 32, 16)); 
        g2.setColor(new Color(255, 0, 0, 150)); g2.drawOval(10, 10, 12, 12); g2.dispose();
        Cursor c = toolkit.createCustomCursor(image, new Point(16, 16), "Crosshair");
        this.setCursor(c);
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(); int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, colorBgStart, w, h, colorBgEnd);
            g2d.setPaint(gp); g2d.fillRect(0, 0, w, h);
        }
    }

    private class ModernButton extends JButton {
        private boolean isRevealed = false; 
        private boolean isFlagged = false;
        private String symbol = ""; 
        private Color symbolColor = Color.WHITE; 
        private boolean isHovered = false;
        private boolean isUsed = false; 
        private Color baseBtnColor; 
        private int fontSize;

        public ModernButton(Color playerColor, int fontSize) {
            this.baseBtnColor = playerColor;
            this.fontSize = fontSize;
            setContentAreaFilled(false); 
            setFocusPainted(false); 
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR)); 
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }
        
        public void setRevealed(boolean r) { this.isRevealed = r; }
        public void setFlagged(boolean f) { this.isFlagged = f; }
        public void setSymbol(String s, Color c) { this.symbol = s; this.symbolColor = c; }
        public void setUsed(boolean u) { this.isUsed = u; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            
            if (isRevealed) { 
                g2.setColor(colorRevealedBg); 
            } else {
                if (isHovered) g2.setColor(new Color(baseBtnColor.getRed(), baseBtnColor.getGreen(), baseBtnColor.getBlue(), 180));
                else g2.setColor(new Color(baseBtnColor.getRed(), baseBtnColor.getGreen(), baseBtnColor.getBlue(), 100));
            }
            
            g2.fill(new RoundRectangle2D.Float(2, 2, w-4, h-4, 15, 15));
            
            if (isHovered && !isRevealed) {
                g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(2, 2, w-4, h-4, 15, 15));
            }
            
            if (isFlagged && !isRevealed) {
                drawCenteredString(g2, "🚩", w, h, new Font("Segoe UI Emoji", Font.PLAIN, fontSize), Color.ORANGE);
            }
            else if (isRevealed) {
                Font f = (symbol.equals("?") || symbol.equals("🎁") || symbol.equals("💣")) 
                        ? new Font("Segoe UI Emoji", Font.PLAIN, fontSize) 
                        : new Font("Segoe UI", Font.BOLD, fontSize);

                if (isUsed && (symbol.equals("?") || symbol.equals("🎁"))) {
                     drawCenteredString(g2, "✔", w, h, f, Color.GREEN.darker());
                } else {
                     drawCenteredString(g2, symbol, w, h, f, symbolColor);
                }
            }
            g2.dispose();
        }
        
        private void drawCenteredString(Graphics2D g, String text, int w, int h, Font font, Color color) {
            if (text == null || text.isEmpty()) return;
            g.setFont(font); g.setColor(color);
            FontMetrics fm = g.getFontMetrics();
            int x = (w - fm.stringWidth(text)) / 2;
            int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
            g.drawString(text, x, y);
        }
    }
}