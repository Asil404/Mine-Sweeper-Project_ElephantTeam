package View;

import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Difficulty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameWindow extends JFrame {

    private GameController controller;
    private Board board;
    private JButton[][] buttons;

    public GameWindow(Difficulty difficulty) {
        // Set up controller and model
        this.controller = new GameController(difficulty);
        this.board = controller.getBoard();

        int rows = board.getRows();
        int cols = board.getCols();

        buttons = new JButton[rows][cols];

        setTitle("MiniSweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(rows, cols));
        initializeButtons(boardPanel, rows, cols);

        add(boardPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);
    }

    private void initializeButtons(JPanel panel, int rows, int cols) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(40, 40));

                final int row = r;
                final int col = c;

                // Mouse listener for left/right click
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            controller.handleLeftClick(row, col);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            controller.handleRightClick(row, col);
                        }

                        refreshBoard();

                        if (controller.isGameOver()) {
                            JOptionPane.showMessageDialog(
                                    GameWindow.this,
                                    "Boom! You hit a mine.",
                                    "Game Over",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    }
                });

                buttons[r][c] = button;
                panel.add(button);
            }
        }
    }

    private void refreshBoard() {
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                JButton button = buttons[r][c];

                if (cell.isRevealed()) {
                    button.setEnabled(false); // can't click again

                    if (cell.isMine()) {
                        button.setText("*");
                    } else {
                        int adj = cell.getAdjacentMines();
                        button.setText(adj == 0 ? "" : String.valueOf(adj));
                    }
                } else if (cell.isFlagged()) {
                    button.setText("F");
                    button.setEnabled(true);
                } else {
                    button.setText("");
                    button.setEnabled(true);
                }
            }
        }
    }

    // Main method to run the game
    public static void main(String[] args) {
        // Simple start with EASY difficulty for Iteration 1
        SwingUtilities.invokeLater(() -> new GameWindow(Difficulty.EASY));
    }
}
