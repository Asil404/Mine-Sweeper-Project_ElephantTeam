package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InstructionsWindow extends JFrame {

    public InstructionsWindow() {
        setTitle("Game Instructions");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // מסך מלא
        setUndecorated(true);                    // ללא מסגרת
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(44, 62, 80));
        
        // --- כותרת ---
        JLabel title = new JLabel("HOW TO PLAY", SwingConstants.CENTER);
        title.setFont(new Font("Verdana", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(30,0,30,0));
        mainPanel.add(title, BorderLayout.NORTH);

        // --- מרכז: גריד שמחזיק את שני הצדדים ---
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 40, 0, 40); // רווח בין הטורים
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // ---------------------------------------------------
        // צד שמאל: חוקים וטקסט
        // ---------------------------------------------------
        JPanel rulesPanel = createSectionPanel("GAME RULES");
        
        JPanel rulesList = new JPanel();
        rulesList.setLayout(new BoxLayout(rulesList, BoxLayout.Y_AXIS));
        rulesList.setOpaque(false);

        rulesList.add(createRuleText("Objective:", "Reveal all safe cells or flag all mines."));
        rulesList.add(Box.createVerticalStrut(20));
        rulesList.add(createRuleText("Lives:", "Shared lives. Hitting a mine costs 1 life."));
        rulesList.add(Box.createVerticalStrut(20));
        rulesList.add(createRuleText("Turns:", "Players take turns. Board clears = Turn switch."));
        rulesList.add(Box.createVerticalStrut(20));
        rulesList.add(createRuleText("Bonuses:", "Questions & Gifts cost points to activate!"));
        rulesList.add(Box.createVerticalStrut(20));
        rulesList.add(createRuleText("Win:", "Clear both boards or flag all mines to win."));
        
        rulesPanel.add(rulesList, BorderLayout.CENTER);
        centerContainer.add(rulesPanel, gbc);

        // ---------------------------------------------------
        // צד ימין: מקרא (אייקונים + כפתורי מערכת)
        // ---------------------------------------------------
        gbc.gridx = 1;
        JPanel legendPanel = createSectionPanel("CONTROLS & LEGEND");
        
        JPanel iconList = new JPanel();
        iconList.setLayout(new BoxLayout(iconList, BoxLayout.Y_AXIS));
        iconList.setOpaque(false);
        
        // 1. אלמנטים במשחק
        iconList.add(createLegendRow("💣", "Mine", "Avoid! (-1 Life)"));
        iconList.add(Box.createVerticalStrut(10));
        iconList.add(createLegendRow("🚩", "Flag", "Right Click to mark."));
        iconList.add(Box.createVerticalStrut(10));
        iconList.add(createLegendRow("🎁", "Gift", "Surprise Box (+/-)."));
        iconList.add(Box.createVerticalStrut(10));
        iconList.add(createLegendRow("❓", "Trivia", "Answer for rewards."));
        
        // קו מפריד
        iconList.add(Box.createVerticalStrut(20));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255,255,255,50));
        iconList.add(sep);
        iconList.add(Box.createVerticalStrut(10));
        
        // 2. כפתורי מערכת (מה שהיה חסר!)
        iconList.add(createLegendRow("🏠", "Home", "Return to Main Menu."));
        iconList.add(Box.createVerticalStrut(10));
        iconList.add(createLegendRow("🔄", "Restart", "Start a new game."));
        iconList.add(Box.createVerticalStrut(10));
        iconList.add(createLegendRow("⏸", "Pause", "Pause game & timer."));
        iconList.add(Box.createVerticalStrut(10));
        iconList.add(createLegendRow("🌗", "Theme", "Light / Dark Mode."));

        legendPanel.add(iconList, BorderLayout.CENTER);
        centerContainer.add(legendPanel, gbc);

        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // --- כפתור סגירה ---
        JButton closeBtn = new JButton("GOT IT!");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        closeBtn.setBackground(new Color(108, 92, 231));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        
        JPanel bottom = new JPanel(); 
        bottom.setOpaque(false); 
        bottom.setBorder(new EmptyBorder(30,0,50,0));
        closeBtn.setPreferredSize(new Dimension(250, 65));
        bottom.add(closeBtn);
        mainPanel.add(bottom, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    // --- פונקציות עזר לעיצוב ---

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255,255,255,50), 2),
            new EmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 26));
        t.setForeground(new Color(85, 239, 196)); // צבע כותרת ירוק-ניאון
        t.setBorder(new EmptyBorder(0,0,20,0));
        p.add(t, BorderLayout.NORTH);
        return p;
    }

    private JLabel createRuleText(String title, String desc) {
        // שימוש ב-HTML כדי לעצב את הכותרת בצבע שונה מהטקסט
        return new JLabel("<html><font color='#81ecec' size='5'><b>" + title + "</b></font> <font color='white' size='5'>" + desc + "</font></html>");
    }

    private JPanel createLegendRow(String icon, String title, String desc) {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(600, 50)); // גובה שורה
        
        // האייקון
        JLabel i = new JLabel(icon);
        i.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        i.setForeground(Color.WHITE);
        i.setPreferredSize(new Dimension(50, 40));
        i.setHorizontalAlignment(SwingConstants.CENTER);
        
        // הטקסט
        JLabel t = new JLabel("<html><font color='#a29bfe' size='5'><b>" + title + ":</b></font> <font color='white' size='4'>" + desc + "</font></html>");
        
        p.add(i, BorderLayout.WEST);
        p.add(t, BorderLayout.CENTER);
        return p;
    }
}