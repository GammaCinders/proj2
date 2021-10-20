package proj2;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class GUI1024Panel2 extends JPanel {

    final int borderThickness = 3;
    private JLabel[][] gameBoardUI;
    private NumberGameArrayList gameLogic;

    private JMenuItem changeWinValue, reset, quit;
    private JButton resizeBoardButton;

    /**Watch me do some swaggy shit with this*/
    private ArrayList<Color> colors = new ArrayList<>();

    public GUI1024Panel2(JMenuItem changeWin,
                         JMenuItem reset, JMenuItem quit,
                         JButton resizeBoardButton) {

        gameLogic = new NumberGameArrayList();
        setBorder(BorderFactory.createLineBorder(Color.ORANGE));

        resetBoard(4, 4, 16, false);

        setFocusable(true);
        addKeyListener(new SlideListener());

        //Setup for stuff from GUI1024
        this.changeWinValue = changeWin;
        this.reset = reset;
        this.quit = quit;
        this.resizeBoardButton = resizeBoardButton;
        this.changeWinValue.addActionListener(new OptionsListener());
        this.reset.addActionListener(new OptionsListener());
        this.quit.addActionListener(new OptionsListener());
        this.resizeBoardButton.addActionListener(new OptionsListener());
    }

    private void resetBoard(int rows, int cols, int winningValue, boolean keepCurrentValues) {
        ArrayList<Cell> oldCells = null;
        if(keepCurrentValues) {
            oldCells = gameLogic.getNonEmptyTiles();
        }

        gameLogic.resizeBoard(rows, cols, winningValue);
        setLayout(new GridLayout(rows, cols));
        gameBoardUI = new JLabel[rows][cols];
        removeAll();

        Font myTextFont = new Font(Font.MONOSPACED, Font.BOLD, 40);
        for (int k = 0; k < gameBoardUI.length; k++) {
            for (int m = 0; m < gameBoardUI[k].length; m++) {
                gameBoardUI[k][m] = new JLabel("",
                        SwingConstants.CENTER);
                gameBoardUI[k][m].setFont(myTextFont);
                gameBoardUI[k][m].setBorder(
                        BorderFactory.createLineBorder(Color.lightGray, borderThickness));
                gameBoardUI[k][m].setForeground(Color.RED);
                gameBoardUI[k][m].setPreferredSize(
                        new Dimension(100, 100));

                setBackground(Color.GRAY);
                add(gameBoardUI[k][m]);
            }
        }

        if(oldCells == null) {
            gameLogic.reset();
        } else {
            gameLogic.setValues(oldCells);
        }

        updateBoard();
    }

    private void updateBoard() {
        for (JLabel[] row : gameBoardUI)
            for (JLabel s : row) {
                s.setBorder(BorderFactory.createLineBorder(Color.lightGray, borderThickness));
                s.setText("");
            }

        ArrayList<Cell> out = gameLogic.getNonEmptyTiles();
        if (out == null) {
            JOptionPane.showMessageDialog(null, "Incomplete implementation getNonEmptyTiles()");
            return;
        }
        for (Cell c : out) {
            if(c.getRow() < gameBoardUI.length && c.getColumn() < gameBoardUI[0].length) {
                JLabel z = gameBoardUI[c.row][c.column];
                z.setText(String.valueOf(Math.abs(c.value)));
                z.setForeground(c.value > 0 ? Color.BLACK : Color.RED);
                z.setBorder(BorderFactory.createLineBorder(generateColor(c.getValue()), borderThickness));
            } else {
                //TODO nothing here rn
            }
        }
    }

    /******************************************************************
     * Sick ass algorithm to create random Colors that usually look
     * nice and are also usually different for as many as are needed.
     * They are stored in colors ArrayList
     * @param value value of tile
     * @return A color based on the number value and its closest
     *          power of 2
     * @throws IllegalArgumentException if value < 2
     */
    private Color generateColor(int value) {
        if(value < 2) {
            throw new IllegalArgumentException();
        }

        int colorNumber;

        //Since numbers go up by powers of 2, have to find
        //roughly the closest power of 2
        for(colorNumber=0; value>1; value/=2) {
            colorNumber++;
        }

        //Generate colors up to the needed number
        if(colorNumber > colors.size()) {
            for(int i=colors.size(); i<colorNumber; i++) {
                int r = 255;
                int g = 255;
                int b = 255;

                //while loop to prevent getting a bunch of gray colors,
                //could be more efficient, but shouldn't matter here
                while(Math.abs(r-g) < 20
                        && Math.abs(g-b) < 20
                        && Math.abs(b-r) < 20) {
                    r = (int)(Math.random()*256);
                    g = (int)(Math.random()*256);
                    b =(int)(Math.random()*256);
                }

                //This averages each random value with 100 to ensure
                //the values are easy to see
                //r = (r + 100)/2;
                //g = (g + 100)/2;
                //b = (b + 100)/2;

                //Try to find a color in a new gray region, gives up
                //after 4 tries at the most
                boolean goodEnough;
                for(int j=0; i<4; i++) {
                    goodEnough = true;

                    for(Color color : colors) {
                        //Check if the average rgb value is similar to one that already exists
                        if(Math.abs((color.getRed() + color.getBlue()
                                + color.getGreen()/3) - ((r+g+b)/3)) < 50) {
                            goodEnough = false;
                        }
                    }

                    if(goodEnough) {
                        break;
                    }
                }


                colors.add(new Color(r, g, b));
            }
        }

        //Return the color for the closest power of 2
        return colors.get(colorNumber-1);
    }

    public void resetBoardWithInput(boolean changeWinValue) {
        int rows, cols, winNum;
        try {
            rows = Integer.parseInt(JOptionPane.showInputDialog(null, "Desired Number of rows"));
            if(rows < 1) {
                throw new IllegalArgumentException();
            }
        } catch(Exception e) {
            rows = 4;
        }

        try {
            cols = Integer.parseInt(JOptionPane.showInputDialog(null, "Desired Number of columns"));
            if(cols < 1) {
                throw new IllegalArgumentException();
            }
        } catch(Exception e) {
            cols = 4;
        }

        if(changeWinValue) {
            try {
                winNum = Integer.parseInt(JOptionPane.showInputDialog(null, "Desired Winning Value"));
                gameLogic.setWinningValue(winNum);
            } catch(Exception e) {
                winNum = 16;
            }
        } else {
            winNum = gameLogic.getWinningValue();
        }


        resetBoard(rows, cols, winNum, true);

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
                        colors = new ArrayList<>();
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
                try {
                    //This 3 lines suck ass to read,
                    //but I have to do it for 'style'
                    gameLogic.setWinningValue(Integer.parseInt(
                                        JOptionPane.showInputDialog(
                                            "Enter new win value")));
                } catch(NumberFormatException notNum) {
                    JOptionPane.showMessageDialog(
                            null, "Input is not a number");
                } catch(IllegalArgumentException illArg) {
                    JOptionPane.showMessageDialog(
                            null, "Input is not a valid power of 2");
                } catch(Exception exc) {
                    //TODO maybe add something here
                }
            } else if(e.getSource() == reset) {
                gameLogic.reset();
                colors = new ArrayList<>();
                updateBoard();
            } else if(e.getSource() == quit) {
                System.exit(1);
            } else if(e.getSource() == resizeBoardButton) {
                resetBoardWithInput(false);
            } else {
                //TODO probably don't need anything here but idk
            }
        }
    }
}