package View;

import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Difficulty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameWindow extends JFrame {

    private GameController controller;
    private JButton[][] buttons1;
    private JButton[][] buttons2;
    private JLabel infoLabel;

    public GameWindow(Difficulty difficulty) {
        this.controller = new GameController(difficulty);

        setTitle("MiniSweeper - Iteration 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Info Panel
        infoLabel = new JLabel();
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        updateInfoLabel();
        add(infoLabel, BorderLayout.NORTH);

        // Center Panel (Two Boards)
        JPanel mainGamePanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainGamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        mainGamePanel.add(createBoardPanel(controller.getBoard1(), 1));
        mainGamePanel.add(createBoardPanel(controller.getBoard2(), 2));

        add(mainGamePanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createBoardPanel(Board board, int playerID) {
        int rows = board.getRows();
        int cols = board.getCols();
        
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Player " + playerID);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols));
        JButton[][] buttonArray = new JButton[rows][cols];

        if (playerID == 1) buttons1 = buttonArray;
        else buttons2 = buttonArray;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(35, 35));
                button.setMargin(new Insets(0,0,0,0));
                button.setFont(new Font("Arial", Font.PLAIN, 10));

                final int row = r;
                final int col = c;

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            controller.handleLeftClick(row, col, playerID);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            controller.handleRightClick(row, col, playerID);
                        }
                        refreshView();
                    }
                });

                buttonArray[r][c] = button;
                gridPanel.add(button);
            }
        }
        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private void updateInfoLabel() {
        String turnText = controller.isPlayer1Turn() ? "Player 1's Turn" : "Player 2's Turn";
        infoLabel.setText(turnText);
        infoLabel.setForeground(controller.isPlayer1Turn() ? new Color(0, 100, 0) : new Color(0, 0, 150));
    }

    private void refreshView() {
        updateInfoLabel();
        updateBoardGrid(controller.getBoard1(), buttons1);
        updateBoardGrid(controller.getBoard2(), buttons2);
    }

    private void updateBoardGrid(Board board, JButton[][] buttons) {
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                JButton button = buttons[r][c];

                if (cell.isRevealed()) {
                    button.setEnabled(false);
                    button.setBackground(Color.WHITE);

                    if (cell.isMine()) {
                        button.setText("M");
                        button.setBackground(Color.RED);
                    } else if (cell.isQuestion()) {
                        button.setText("?");
                        button.setBackground(Color.ORANGE);
                    } else if (cell.isSurprise()) {
                        button.setText("!");
                        button.setBackground(Color.PINK);
                    } else {
                        int adj = cell.getAdjacentMines();
                        button.setText(adj > 0 ? String.valueOf(adj) : "");
                    }
                } else if (cell.isFlagged()) {
                    button.setText("F");
                    button.setBackground(Color.YELLOW);
                    button.setEnabled(true);
                } else {
                    button.setText("");
                    button.setBackground(null);
                    button.setEnabled(true);
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow(Difficulty.MEDIUM));
    }
}