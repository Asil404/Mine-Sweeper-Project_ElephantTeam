package View;

import Model.Question;
import Model.SysData;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdminWindow extends JFrame {

    // צבעים מודרניים
    private final Color COLOR_BG_START = new Color(44, 62, 80);
    private final Color COLOR_BG_END = new Color(0, 0, 0);
    private final Color COLOR_TABLE_HEADER = new Color(108, 92, 231);
    private final Color COLOR_TABLE_ROW = new Color(60, 60, 60);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_ACCENT = new Color(85, 239, 196);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField questionField;
    private JTextField[] answerFields;
    private JComboBox<String> correctAnsBox;
    private JComboBox<String> levelBox;
    
    // אנימציה
    private List<Particle> particles = new ArrayList<>();
    private Timer animTimer;

    public AdminWindow() {
        setTitle("Admin Dashboard - Question Manager");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // מסך מלא
        setUndecorated(true);                    // ללא מסגרת
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initParticles();

        // פאנל ראשי עם רקע מונפש
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, h, COLOR_BG_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                
                drawGrid(g2, w, h);
                drawParticles(g2);
            }
        };
        setContentPane(mainPanel);

        animTimer = new Timer(30, e -> { updateParticles(); mainPanel.repaint(); });
        animTimer.start();

        // --- כותרת עליונה ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(30, 0, 20, 0));
        
        JLabel title = new JLabel("QUESTIONS MANAGEMENT");
        title.setFont(new Font("Verdana", Font.BOLD, 36));
        title.setForeground(COLOR_TEXT);
        topPanel.add(title);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- מרכז: טבלה ---
        setupTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // הורדת מסגרת כחולה
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        // עטיפה לפאנל הטבלה כדי לתת לה שוליים
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(10, 100, 10, 100)); // שוליים רחבים בצדדים
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(tableWrapper, BorderLayout.CENTER);

        // --- למטה: טופס הוספה וכפתורים ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(20, 100, 40, 100));

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 20, 15));
        formPanel.setOpaque(false);

        // יצירת שדות מעוצבים
        questionField = createStyledInput("Question Text...");
        answerFields = new JTextField[4];
        for(int i=0; i<4; i++) answerFields[i] = createStyledInput("Answer " + (i+1));
        
        correctAnsBox = createStyledCombo(new String[]{"Correct: A", "Correct: B", "Correct: C", "Correct: D"});
        levelBox = createStyledCombo(new String[]{"Easy", "Medium", "Hard", "Expert"});

        // הוספה לגריד
        formPanel.add(createLabel("Question:")); formPanel.add(questionField);
        formPanel.add(createLabel("Difficulty:")); formPanel.add(levelBox);
        
        formPanel.add(createLabel("Answer A:")); formPanel.add(answerFields[0]);
        formPanel.add(createLabel("Answer B:")); formPanel.add(answerFields[1]);
        
        formPanel.add(createLabel("Answer C:")); formPanel.add(answerFields[2]);
        formPanel.add(createLabel("Answer D:")); formPanel.add(answerFields[3]);
        
        formPanel.add(createLabel("Correct Answer:")); formPanel.add(correctAnsBox);
        formPanel.add(new JLabel()); formPanel.add(new JLabel()); // סתם רווח

        bottomPanel.add(formPanel, BorderLayout.CENTER);

        // כפתורים
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        btnPanel.setOpaque(false);

        JButton addBtn = createStyledButton("ADD QUESTION", new Color(0, 184, 148));
        addBtn.addActionListener(e -> addQuestion());
        
        JButton delBtn = createStyledButton("DELETE SELECTED", new Color(214, 48, 49));
        delBtn.addActionListener(e -> deleteQuestion());
        
        JButton backBtn = createStyledButton("BACK TO MENU", new Color(108, 92, 231));
        backBtn.addActionListener(e -> {
            animTimer.stop();
            dispose();
            new WelcomeWindow();
        });

        btnPanel.add(addBtn);
        btnPanel.add(delBtn);
        btnPanel.add(backBtn);
        
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // --- עיצוב טבלה ---
    private void setupTable() {
        String[] cols = {"ID", "Question", "Level", "Correct Answer"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(255, 255, 255, 10)); // שקוף למחצה
        table.setSelectionBackground(COLOR_ACCENT);
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(COLOR_TABLE_HEADER);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        center.setOpaque(false);
        center.setForeground(Color.WHITE);
        for(int i=0; i<4; i++) table.getColumnModel().getColumn(i).setCellRenderer(center);
        
        // טעינת מידע
        loadTableData();
    }
    
    // --- לוגיקת נתונים ---
    private void loadTableData() {
        tableModel.setRowCount(0);
        List<Question> list = SysData.getInstance().getQuestions();
        for (int i = 0; i < list.size(); i++) {
            Question q = list.get(i);
            String correctStr = q.getAnswers()[q.getCorrectAnsIndex()];
            tableModel.addRow(new Object[]{i + 1, q.getQuestionText(), q.getLevel(), correctStr});
        }
    }
    
    private void addQuestion() {
        String text = questionField.getText().trim();
        boolean empty = text.isEmpty();
        String[] answers = new String[4];
        for(int i=0; i<4; i++) {
            answers[i] = answerFields[i].getText().trim();
            if(answers[i].isEmpty()) empty = true;
        }
        
        if (empty) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        Question q = new Question(text, answers, correctAnsBox.getSelectedIndex(), (String)levelBox.getSelectedItem());
        SysData.getInstance().addQuestion(q);
        loadTableData();
        clearFields();
    }
    
    private void deleteQuestion() {
        int row = table.getSelectedRow();
        if(row == -1) return;
        SysData.getInstance().removeQuestion(row);
        loadTableData();
    }
    
    private void clearFields() {
        questionField.setText("");
        for(JTextField f : answerFields) f.setText("");
    }

    // --- כלי עזר לעיצוב ---
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(new Color(200, 200, 200));
        return l;
    }
    
    private JTextField createStyledInput(String hint) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(COLOR_ACCENT);
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 100, 100)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return tf;
    }
    
    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return box;
    }
    
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),25,25));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 45));
        return btn;
    }

    private void drawGrid(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(255, 255, 255, 15));
        for(int x=0; x<w; x+=50) g2.drawLine(x,0,x,h);
        for(int y=0; y<h; y+=50) g2.drawLine(0,y,w,y);
    }
    private void initParticles() {
        Random r = new Random();
        for(int i=0; i<30; i++) particles.add(new Particle(r.nextInt(1920), r.nextInt(1080)));
    }
    private void updateParticles() {
        for(Particle p : particles) {
            p.y -= p.speed;
            if(p.y < -20) p.y = getHeight();
        }
    }
    private void drawParticles(Graphics2D g2) {
        g2.setColor(new Color(255,255,255,30));
        for(Particle p : particles) g2.fillOval(p.x, p.y, p.size, p.size);
    }
    private class Particle {
        int x, y, size, speed;
        Particle(int x, int y) { this.x=x; this.y=y; size=new Random().nextInt(5)+3; speed=new Random().nextInt(3)+1; }
    }
}