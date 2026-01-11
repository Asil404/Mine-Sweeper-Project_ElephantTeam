package View;

import Model.Question;
import Model.SysData;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet; // חשוב!

public class AdminWindow extends JFrame {

    // --- צבעים ---
    private final Color COLOR_BG_START = new Color(44, 62, 80);
    private final Color COLOR_BG_END = new Color(0, 0, 0);
    private final Color COLOR_GLASS = new Color(0, 0, 0, 100);
    private final Color COLOR_SELECTION = new Color(0, 100, 255, 200); 
    private final Color COLOR_HOVER = new Color(255, 255, 255, 30);
    private final Color COLOR_TEXT = Color.WHITE;
    private final Color COLOR_ACCENT = new Color(85, 239, 196); 
    private final Color COLOR_INPUT_BG = new Color(255, 255, 255, 20);

    // --- רכיבים ---
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JTextField questionField;
    private JTextField[] answerFields;
    private JComboBox<String> correctAnsBox;
    private JComboBox<String> levelBox;
    private JButton submitBtn;

    private int rollOverRowIndex = -1; 
    private int editingRowIndex = -1;  
    
    private List<Particle> particles = new ArrayList<>();
    private Timer animTimer;

    public AdminWindow() {
        setTitle("Admin Dashboard - Question Manager");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initParticles();

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

        JLabel title = new JLabel("SYSTEM MANAGEMENT");
        title.setFont(new Font("Verdana", Font.BOLD, 36));
        title.setForeground(COLOR_TEXT);
        title.setBorder(new EmptyBorder(30, 0, 20, 0));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(0, 80, 20, 80));

        // --- טבלה ---
        JPanel tableCard = createGlassPanel();
        tableCard.setLayout(new BorderLayout());
        
        JLabel tableTitle = new JLabel(" QUESTIONS DATABASE (Left/Right Click to Select)");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(new Color(200, 200, 200));
        tableTitle.setBorder(new EmptyBorder(10, 15, 5, 10));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        setupTable(); 
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(0,0,0,0));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // --- טופס ---
        JPanel formCard = createGlassPanel();
        formCard.setLayout(new GridBagLayout()); 
        initFormComponents(formCard); 

        centerContainer.add(tableCard);
        centerContainer.add(Box.createVerticalStrut(20));
        centerContainer.add(formCard);

        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // 4. כפתורים
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        btnPanel.setOpaque(false);

        submitBtn = createStyledButton("ADD QUESTION", new Color(46, 204, 113));
        submitBtn.addActionListener(e -> submitQuestion());
        
        JButton editBtn = createStyledButton("EDIT SELECTED", new Color(243, 156, 18));
        editBtn.addActionListener(e -> loadQuestionForEdit());

        JButton delBtn = createStyledButton("DELETE SELECTED", new Color(231, 76, 60));
        delBtn.addActionListener(e -> deleteQuestion());
        
        JButton clearBtn = createStyledButton("CLEAR", new Color(149, 165, 166));
        clearBtn.addActionListener(e -> clearFields());

        JButton backBtn = createStyledButton("BACK TO MENU", new Color(155, 89, 182));
        backBtn.addActionListener(e -> {
            animTimer.stop();
            dispose();
            new WelcomeWindow(); 
        });

        btnPanel.add(submitBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(backBtn);
        
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        loadTableData(); 
        setVisible(true);
    }

    private void setupTable() {
        String[] cols = {"ID", "Question", "Difficulty", "Correct Answer"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; } 
        };

        table = new JTable(tableModel);
        table.setFocusable(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != -1) {
                    table.setRowSelectionInterval(row, row);
                    table.repaint(); 
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                rollOverRowIndex = -1;
                table.repaint();
            }
        });

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != rollOverRowIndex) {
                    rollOverRowIndex = row;
                    table.repaint(); 
                }
            }
        });

        table.setOpaque(false);
        table.setBackground(new Color(0,0,0,0));
        table.setForeground(Color.WHITE);
        table.setRowHeight(40); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(new Color(108, 92, 231));
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 14));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });
        header.setPreferredSize(new Dimension(0, 40));

        SolidSelectionRenderer customRenderer = new SolidSelectionRenderer();
        for(int i=0; i<table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
        
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
    }

    private class SolidSelectionRenderer extends DefaultTableCellRenderer {
        private boolean isSelectedRow = false;
        private boolean isHoverRow = false;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.isSelectedRow = isSelected;
            this.isHoverRow = (row == rollOverRowIndex);

            Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setForeground(Color.WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(null);

            if (column == 3 && !isSelected) setForeground(COLOR_ACCENT);
            if (isSelected) setFont(new Font("Segoe UI", Font.BOLD, 15));
            else setFont(new Font("Segoe UI", Font.PLAIN, 15));

            return c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isSelectedRow) {
                g.setColor(COLOR_SELECTION); 
                g.fillRect(0, 0, getWidth(), getHeight());
            } else if (isHoverRow) {
                g.setColor(COLOR_HOVER);
                g.fillRect(0, 0, getWidth(), getHeight());
            } 
            super.paintComponent(g);
        }
    }

    private void showCustomMessage(String title, String message, boolean isError) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.setColor(isError ? new Color(231, 76, 60) : new Color(46, 204, 113));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
            }
        };
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel(isError ? "⚠️" : "✅");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setForeground(Color.WHITE);

        JTextArea msgLabel = new JTextArea(message);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setOpaque(false);
        msgLabel.setWrapStyleWord(true);
        msgLabel.setLineWrap(true);
        msgLabel.setEditable(false);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton okBtn = createStyledButton("OK", isError ? new Color(231, 76, 60) : new Color(46, 204, 113));
        okBtn.setPreferredSize(new Dimension(100, 40));
        okBtn.addActionListener(e -> dialog.dispose());
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(okBtn);

        content.add(icon, BorderLayout.NORTH);
        content.add(msgLabel, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
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

        JLabel icon = new JLabel("🗑️");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton yesBtn = createStyledButton("YES, DELETE", new Color(231, 76, 60));
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

    private void loadQuestionForEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showCustomMessage("No Selection", "Please select a question to edit first!", true);
            return;
        }

        List<Question> list = SysData.getInstance().getQuestions();
        if (row >= list.size()) return;
        
        Question q = list.get(row);
        questionField.setText(q.getQuestionText());
        String[] answers = q.getAnswers();
        for(int i=0; i<4; i++) {
            if (i < answers.length) answerFields[i].setText(answers[i]);
        }
        
        correctAnsBox.setSelectedIndex(q.getCorrectAnsIndex());
        String savedLevel = q.getLevel();
        boolean found = false;
        for (int i = 0; i < levelBox.getItemCount(); i++) {
            String item = levelBox.getItemAt(i);
            if (item.contains(savedLevel)) {
                levelBox.setSelectedIndex(i);
                found = true; break;
            }
        }
        if (!found) try { levelBox.setSelectedItem(savedLevel); } catch (Exception e) {}

        editingRowIndex = row;
        submitBtn.setText("UPDATE QUESTION");
        submitBtn.setBackground(new Color(243, 156, 18)); 
    }

    private void submitQuestion() {
        String text = questionField.getText().trim();
        boolean empty = text.isEmpty();
        String[] answers = new String[4];
        
        Set<String> uniqueAnswers = new HashSet<>();
        boolean hasDuplicates = false;

        for(int i=0; i<4; i++) {
            answers[i] = answerFields[i].getText().trim();
            if(answers[i].isEmpty()) empty = true;
            
            if (!uniqueAnswers.add(answers[i].toLowerCase())) { 
                hasDuplicates = true;
            }
        }
        
        if (empty) {
            showCustomMessage("Error", "Please fill all fields!", true);
            return;
        }

        if (hasDuplicates) {
            showCustomMessage("Duplicate Answers", "Duplicate answers are not allowed!", true);
            return;
        }
        
        String levelRaw = (String) levelBox.getSelectedItem(); 
        Question q = new Question(text, answers, correctAnsBox.getSelectedIndex(), levelRaw);
        
        if (editingRowIndex == -1) {
            SysData.getInstance().addQuestion(q);
            showCustomMessage("Success", "Question added successfully!", false);
        } else {
            List<Question> list = SysData.getInstance().getQuestions();
            list.set(editingRowIndex, q); 
            showCustomMessage("Success", "Question updated successfully!", false);
        }
        
        loadTableData();
        clearFields();
    }
    
    private void deleteQuestion() {
        int row = table.getSelectedRow();
        if(row == -1) {
            showCustomMessage("No Selection", "Please select a question to delete first!", true);
            return;
        }
        
        if (showCustomConfirm("Confirm Deletion", "Are you sure you want to delete this question?")) {
            SysData.getInstance().removeQuestion(row);
            
            if (editingRowIndex == row) {
                clearFields();
            } else if (editingRowIndex > row) {
                editingRowIndex--;
            }
            
            loadTableData();
            showCustomMessage("Deleted", "Question deleted successfully.", false);
        }
    }
    
    private void clearFields() {
        questionField.setText("");
        for(JTextField f : answerFields) f.setText("");
        correctAnsBox.setSelectedIndex(0);
        levelBox.setSelectedIndex(0);
        
        editingRowIndex = -1;
        submitBtn.setText("ADD QUESTION");
        submitBtn.setBackground(new Color(46, 204, 113));
        
        table.clearSelection();
    }

    private void initFormComponents(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1; panel.add(createLabel("Question Text:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 3;
        questionField = createStyledInput(); panel.add(questionField, gbc);
        
        gbc.gridwidth = 1; gbc.weightx = 0.1;
        gbc.gridx = 0; gbc.gridy = 1; panel.add(createLabel("Answer A:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.4; answerFields = new JTextField[4]; answerFields[0] = createStyledInput(); panel.add(answerFields[0], gbc);
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.1; panel.add(createLabel("Answer B:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.4; answerFields[1] = createStyledInput(); panel.add(answerFields[1], gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.1; panel.add(createLabel("Answer C:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.4; answerFields[2] = createStyledInput(); panel.add(answerFields[2], gbc);
        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.1; panel.add(createLabel("Answer D:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0.4; answerFields[3] = createStyledInput(); panel.add(answerFields[3], gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.1; panel.add(createLabel("Correct Answer:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.4; correctAnsBox = createStyledCombo(new String[]{"Answer A", "Answer B", "Answer C", "Answer D"}); panel.add(correctAnsBox, gbc);
        gbc.gridx = 2; gbc.gridy = 3; gbc.weightx = 0.1; panel.add(createLabel("Difficulty:"), gbc);
        gbc.gridx = 3; gbc.gridy = 3; gbc.weightx = 0.4; levelBox = createStyledCombo(new String[]{"1 (Easy)", "2 (Medium)", "3 (Hard)"}); panel.add(levelBox, gbc);
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        List<Question> list = SysData.getInstance().getQuestions();
        if (list == null) return;
        for (int i = 0; i < list.size(); i++) {
            Question q = list.get(i);
            String correctStr = "";
            if (q.getAnswers() != null && q.getCorrectAnsIndex() >= 0 && q.getCorrectAnsIndex() < q.getAnswers().length) {
                correctStr = q.getAnswers()[q.getCorrectAnsIndex()];
            }
            tableModel.addRow(new Object[]{i + 1, q.getQuestionText(), q.getLevel(), correctStr});
        }
    }

    private JPanel createGlassPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_GLASS);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                g2.setColor(new Color(255, 255, 255, 30));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 30, 30));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(200, 200, 200));
        return l;
    }
    
    private JTextField createStyledInput() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(COLOR_ACCENT);
        tf.setOpaque(false);
        tf.setBackground(COLOR_INPUT_BG);
        tf.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 100)), new EmptyBorder(5, 5, 5, 5)));
        return tf;
    }
    
    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(new Color(60, 60, 60));
        box.setForeground(Color.WHITE);
        return box;
    }
    
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),40,40));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 45));
        return btn;
    }

    private void drawGrid(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(255, 255, 255, 10));
        for(int x=0; x<w; x+=60) g2.drawLine(x,0,x,h);
        for(int y=0; y<h; y+=60) g2.drawLine(0,y,w,y);
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