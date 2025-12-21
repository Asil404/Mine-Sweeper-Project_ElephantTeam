package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class CustomDialog extends JDialog {

    public static final int YES_OPTION = 1;
    public static final int NO_OPTION = 0;
    
    private int userSelection = NO_OPTION;
    private final Color BG_COLOR = new Color(44, 62, 80);
    private final Color BORDER_COLOR = new Color(108, 92, 231); // סגול ניאון

    public CustomDialog(JFrame parent, String title, String message, boolean isConfirm) {
        super(parent, true); // Modal = חוסם את החלון שמתחתיו
        setUndecorated(true); // בלי מסגרת מכוערת
        setBackground(new Color(0,0,0,0)); // רקע שקוף כדי לאפשר פינות עגולות

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // רקע כהה
                g2.setColor(BG_COLOR);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                
                // מסגרת צבעונית דקה
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 20, 20));
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // --- כותרת ---
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(162, 155, 254));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // --- הודעה ---
        JLabel msgLabel = new JLabel("<html><center>" + message + "</center></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        msgLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        mainPanel.add(msgLabel, BorderLayout.CENTER);
        
        // --- כפתורים ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        
        if (isConfirm) {
            JButton yesBtn = createBtn("YES", new Color(46, 213, 115));
            yesBtn.addActionListener(e -> { userSelection = YES_OPTION; dispose(); });
            
            JButton noBtn = createBtn("NO", new Color(255, 71, 87));
            noBtn.addActionListener(e -> { userSelection = NO_OPTION; dispose(); });
            
            btnPanel.add(yesBtn);
            btnPanel.add(noBtn);
        } else {
            JButton okBtn = createBtn("OK", new Color(108, 92, 231));
            okBtn.addActionListener(e -> dispose());
            btnPanel.add(okBtn);
        }
        
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(parent); // מרכוז
    }
    
    // פונקציה סטטית נוחה להודעת אישור (כמו JOptionPane)
    public static int showConfirm(JFrame parent, String title, String msg) {
        CustomDialog dlg = new CustomDialog(parent, title, msg, true);
        dlg.setVisible(true);
        return dlg.userSelection;
    }
    
    // פונקציה סטטית להודעת מידע
    public static void showMessage(JFrame parent, String title, String msg) {
        CustomDialog dlg = new CustomDialog(parent, title, msg, false);
        dlg.setVisible(true);
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),15,15));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}