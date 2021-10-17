package proj2;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class GUI1024Panel2 extends JPanel {

    private JLabel[][] gameBoardUI;
    private NumberGameArrayList gameLogic;

    private JMenuItem changeWinValue, reset, quit;

    public GUI1024Panel2(JMenuItem changeWin, JMenuItem reset, JMenuItem quit) {
        setSize(800, 800);
        gameLogic = new NumberGameArrayList();
        gameLogic.resizeBoard(4, 4, 16);

        setBorder(BorderFactory.createLineBorder(Color.ORANGE));
        setLayout(new GridLayout(4, 4));

        gameBoardUI = new JLabel[4][4];

        Font myTextFont = new Font(Font.SERIF, Font.BOLD, 40);
        for (int k = 0; k < gameBoardUI.length; k++)
            for (int m = 0; m < gameBoardUI[k].length; m++) {
                gameBoardUI[k][m] = new JLabel();
                gameBoardUI[k][m].setFont(myTextFont);
                gameBoardUI[k][m].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                gameBoardUI[k][m].setPreferredSize(new Dimension(100, 100));
                add(gameBoardUI[k][m]);
            }

        gameLogic.reset();
        updateBoard();
        setFocusable(true);
        addKeyListener(new SlideListener());

        //Setup for stuff from GUI1024
        this.changeWinValue = changeWin;
        this.reset = reset;
        this.quit = quit;
        this.changeWinValue.addActionListener(new OptionsListener());
        this.reset.addActionListener(new OptionsListener());
        this.quit.addActionListener(new OptionsListener());
    }

    private void updateBoard() {
        for (JLabel[] row : gameBoardUI)
            for (JLabel s : row) {
                s.setText("");
            }

        ArrayList<Cell> out = gameLogic.getNonEmptyTiles();
        if (out == null) {
            JOptionPane.showMessageDialog(null, "Incomplete implementation getNonEmptyTiles()");
            return;
        }
        for (Cell c : out) {
            JLabel z = gameBoardUI[c.row][c.column];
            z.setText(String.valueOf(Math.abs(c.value)));
            z.setForeground(c.value > 0 ? Color.BLACK : Color.RED);
        }
    }

    private class SlideListener implements KeyListener, ActionListener {
        @Override
        public void keyTyped(KeyEvent e) { }

        @Override
        public void keyPressed(KeyEvent e) {
            boolean moved = false;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    moved = gameLogic.slide(SlideDirection.UP);
                    break;
                case KeyEvent.VK_LEFT:
                    moved = gameLogic.slide(SlideDirection.LEFT);
                    break;
                case KeyEvent.VK_DOWN:
                    moved = gameLogic.slide(SlideDirection.DOWN);
                    break;
                case KeyEvent.VK_RIGHT:
                    moved = gameLogic.slide(SlideDirection.RIGHT);
                    break;
                case KeyEvent.VK_U:
                    try {
                        System.out.println("Attempt to undo");
                        gameLogic.undo();
                        moved = true;
                    } catch (IllegalStateException exp) {
                        JOptionPane.showMessageDialog(null, "Can't undo beyond the first move");
                        moved = false;
                    }
            }
            if (moved) {
                updateBoard();
                if (gameLogic.getStatus().equals(GameStatus.USER_WON))
                    JOptionPane.showMessageDialog(null, "You won");
                else if (gameLogic.getStatus().equals(GameStatus.USER_LOST)) {
                    int resp = JOptionPane.showConfirmDialog(null, "Do you want to play again?", "TentOnly Over!",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (resp == JOptionPane.YES_OPTION) {
                        gameLogic.reset();
                        updateBoard();
                    } else {
                        System.exit(0);
                    }
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) { }

        @Override
        public void actionPerformed(ActionEvent e) { }

    }

    private class OptionsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == changeWinValue) {
                //TODO this is fairly hard I think, I'll see
            } else if(e.getSource() == reset) {
                gameLogic.reset();
                updateBoard();
            } else if(e.getSource() == quit) {
                System.exit(1);
            } else {
                //TODO probably don't need anything here but idk
            }
        }
    }
}