package View;

import Model.GameRecord;
import Model.HistoryManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HistoryWindow extends JFrame {

    // --- צבעים ---
    private final Color COLOR_BG_START = new Color(44, 62, 80);
    private final Color COLOR_BG_END = new Color(0, 0, 0);
    private final Color COLOR_TABLE_HEADER = new Color(108, 92, 231);
    private final Color COLOR_TEXT_HEADER = new Color(255, 255, 255);
    private final Color COLOR_HOVER_ROW = new Color(255, 255, 255, 20); 
    private final Color COLOR_ACCENT = new Color(85, 239, 196); 

    private JTable table;
    private DefaultTableModel tableModel;
    private int rollOverRowIndex = -1; 

    public HistoryWindow() {
        setTitle("Mission Log 📂");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // --- פאנל ראשי ---
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, COLOR_BG_START, 0, h, COLOR_BG_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);

                // רשת עדינה
                g2d.setColor(new Color(255, 255, 255, 10));
                for (int x = 0; x < w; x += 60) g2d.drawLine(x, 0, x, h);
                for (int y = 0; y < h; y += 60) g2d.drawLine(0, y, w, y);
            }
        };
        setContentPane(mainPanel);

        // --- כותרת עליונה ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(30, 40, 20, 40));

        JButton backBtn = createStyledButton("BACK", new Color(155, 89, 182));
        backBtn.setPreferredSize(new Dimension(100, 40));
        backBtn.addActionListener(e -> dispose());

        JLabel titleLabel = new JLabel("TEAM HISTORY LOG");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        topPanel.add(backBtn, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(Box.createHorizontalStrut(100), BorderLayout.EAST); 

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- טבלה ---
        setupTable();
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(0,0,0,0));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 60, 20, 60));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- פאנל כפתורים תחתון ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        JButton exportBtn = createStyledButton("EXPORT TO CSV", new Color(46, 204, 113));
        exportBtn.setPreferredSize(new Dimension(200, 45));
        exportBtn.addActionListener(e -> exportHistory());

        JButton clearBtn = createStyledButton("CLEAR HISTORY", new Color(231, 76, 60));
        clearBtn.setPreferredSize(new Dimension(200, 45));
        clearBtn.addActionListener(e -> clearHistoryData());

        bottomPanel.add(exportBtn);
        bottomPanel.add(clearBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        loadData(); 
        setVisible(true);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<GameRecord> history = HistoryManager.getInstance().getRecords();
        if (history == null) return;

        for (GameRecord r : history) {
            String rawWinner = r.getWinner();
            String displayResult;
            if (rawWinner != null && !rawWinner.isEmpty() && !rawWinner.equalsIgnoreCase("null") && !rawWinner.contains("Computer")) {
                displayResult = "VICTORY"; 
            } else {
                displayResult = "DEFEAT";
            }

            tableModel.addRow(new Object[]{
                r.getDate(), r.getP1Name(), r.getP2Name(), displayResult, r.getScore(), r.getDifficulty()
            });
        }
    }

    // ==================================================================================
    //                  לוגיקת ייצוא סטנדרטית (ללא פתיחת תיקייה אוטומטית)
    // ==================================================================================

    private void exportHistory() {
        if (tableModel.getRowCount() == 0) {
            showCustomMessage("Export Failed", "There is no history to export!", true);
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Mission Log");
        
        // הגדרת שם קובץ ברירת מחדל
        fileChooser.setSelectedFile(new File("History_Log.csv"));
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave))) {
                // כותרות
                for (int i = 0; i < table.getColumnCount(); i++) {
                    bw.write(table.getColumnName(i));
                    if (i < table.getColumnCount() - 1) bw.write(",");
                }
                bw.newLine();

                // נתונים
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        Object val = table.getValueAt(i, j);
                        bw.write(val != null ? val.toString() : "");
                        if (j < table.getColumnCount() - 1) bw.write(",");
                    }
                    bw.newLine();
                }

                // רק הודעה, בלי לפתוח את התיקייה
                showCustomMessage("Success", "File saved successfully!", false);

            } catch (IOException ex) {
                showCustomMessage("Error", "Error saving file: " + ex.getMessage(), true);
            }
        }
    }

    // --- מחיקת היסטוריה ---
    private void clearHistoryData() {
        if (tableModel.getRowCount() == 0) {
            showCustomMessage("Info", "History is already empty.", false);
            return;
        }

        if (showCustomConfirm("Clear History", "Are you sure you want to delete ALL history records?\nThis cannot be undone.")) {
            HistoryManager.getInstance().clearAll(); 
            loadData();
            showCustomMessage("Deleted", "History has been cleared and saved.", false);
        }
    }

    private void setupTable() {
        String[] columns = {"Date", "Player 1", "Player 2", "Result", "Score", "Difficulty"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setOpaque(false);
        table.setBackground(new Color(0,0,0,0));
        table.setForeground(Color.WHITE);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        table.setFocusable(false);
        table.setRowSelectionAllowed(true);
        table.getTableHeader().setReorderingAllowed(false);

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
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                rollOverRowIndex = -1;
                table.repaint();
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(COLOR_TABLE_HEADER);
                l.setForeground(COLOR_TEXT_HEADER);
                l.setFont(new Font("Verdana", Font.BOLD, 14));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                return l;
            }
        });
        header.setPreferredSize(new Dimension(0, 50));

        GlassRenderer renderer = new GlassRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private class GlassRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            
            if (row == rollOverRowIndex) {
                setBackground(COLOR_HOVER_ROW);
            } else {
                setBackground(row % 2 == 0 ? new Color(255,255,255,15) : new Color(255,255,255,5));
            }
            
            setForeground(Color.WHITE);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(null);
            
            if (column == 3) {
                String text = value != null ? value.toString() : "";
                if (text.equals("VICTORY")) {
                    setForeground(new Color(46, 204, 113)); 
                    setFont(new Font("Segoe UI", Font.BOLD, 16));
                } else {
                    setForeground(new Color(231, 76, 60)); 
                    setFont(new Font("Segoe UI", Font.BOLD, 16));
                }
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 16));
            }
            
            return c;
        }
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
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
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showCustomMessage(String title, String message, boolean isError) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(isError ? new Color(231, 76, 60) : new Color(46, 204, 113));
                g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
            }
        };
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel(isError ? "⚠️" : "💾");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setForeground(Color.WHITE);

        JLabel msgLabel = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msgLabel.setForeground(Color.WHITE);

        JButton okBtn = createStyledButton("OK", isError ? new Color(231, 76, 60) : new Color(46, 204, 113));
        okBtn.setPreferredSize(new Dimension(100, 40));
        okBtn.addActionListener(e -> dialog.dispose());
        
        JPanel btnPanel = new JPanel(); btnPanel.setOpaque(false); btnPanel.add(okBtn);

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
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, COLOR_BG_START, 0, getHeight(), COLOR_BG_END));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 100, 100));
                g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
            }
        };
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel("🗑️");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel msgLabel = new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        msgLabel.setForeground(Color.WHITE);

        JButton yesBtn = createStyledButton("YES, DELETE", new Color(231, 76, 60));
        yesBtn.setPreferredSize(new Dimension(150, 40));
        yesBtn.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JButton noBtn = createStyledButton("CANCEL", new Color(149, 165, 166));
        noBtn.setPreferredSize(new Dimension(150, 40));
        noBtn.addActionListener(e -> { result[0] = false; dialog.dispose(); });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false); btnPanel.add(yesBtn); btnPanel.add(noBtn);

        content.add(icon, BorderLayout.NORTH);
        content.add(msgLabel, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
        return result[0];
    }
}