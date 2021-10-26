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


    private void resetBoard(int rows, int cols,
                            int winningValue, boolean saveBoard) {
        ArrayList<ArrayList<Cell>> boardHistory =
                saveBoard ? gameLogic.getAllMoves() : null;

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
                        BorderFactory.createLineBorder(
                                Color.lightGray, borderThickness));
                gameBoardUI[k][m].setForeground(Color.RED);
                gameBoardUI[k][m].setPreferredSize(
                        new Dimension(100, 100));

                setBackground(Color.GRAY);
                add(gameBoardUI[k][m]);
            }
        }

        if(boardHistory == null) {
            gameLogic.reset();
        } else {
            gameLogic.setAllMoves(boardHistory);
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

                boolean goodEnough = true;

                //max of 5 tries to generate a definitely unique color
                for(int t=0; t<5; t++) {
                    //while loop to prevent getting a bunch of gray colors,
                    //could be more efficient, but shouldn't matter here
                    while(Math.abs(r-g) < 20
                            && Math.abs(g-b) < 20
                            && Math.abs(b-r) < 20) {
                        r = (int)(Math.random()*256);
                        g = (int)(Math.random()*256);
                        b =(int)(Math.random()*256);
                    }

                    for(Color color : colors) {
                        //Check if the average rgb value is similar to one that already exists
                        if(Math.abs((color.getRed() + color.getBlue()
                                + color.getGreen()/3) - ((r+g+b)/3)) < 20) {
                            goodEnough = false;
                            break;
                        }
                    }

                    //Break early if color is unique
                    if(goodEnough) {break;}
                }

                colors.add(new Color(r, g, b));
            }
        }

        //Return the color for the closest power of 2
        return colors.get(colorNumber-1);
    }

    /******************************************************************
     * calls change methods for rows, columns, and winValue (if
     * changeWinValue arg is true)
     * @param changeWinValue whether to prompt the user to
     *                       change the win value or not
     */
    public void resetBoardWithInput(boolean changeWinValue, boolean showWarning) {
        int newRows = getNewNumberOfRows(showWarning);
        int newColumns = getNewNumberOfColumns(showWarning);
        //I found out what '?' does, so I thought I would try using it
        int newWinValue = changeWinValue
                ? getNewWinValue() : gameLogic.getWinningValue();
        boolean saveBoard = newRows >= gameLogic.getRows()
                && newColumns >= gameLogic.getColumns();

        resetBoard(newRows, newColumns, newWinValue, saveBoard);
    }

    /******************************************************************
     * Sets the value needed to win based on JOptionPane user input
     *****************************************************************/
    public int getNewWinValue() {
        int winNum;

        while(true) {
            try {
                winNum = Integer.parseInt(JOptionPane.showInputDialog(
                        null, "Desired Winning Value"));
                if(!NumberGameArrayList.validPowerOf2(winNum)) {
                    throw new IllegalArgumentException();
                }
            } catch(Exception e) {
                winNum = 0;
                JOptionPane.showMessageDialog(
                        null, "Please enter a valid power of 2");
            }

            if(winNum != 0) {break;}
        }

        return winNum;
    }

    /******************************************************************
     * gets the desired number of rows based on JOptionPane user input
     * @param showWarning whether to show an additional warning
     *                    about resetting the board
     * @return int number of desired rows from the user
     *****************************************************************/
    public int getNewNumberOfRows(boolean showWarning) {
        int rows;

        while(true) {
            try {
                String message = "Desired number of rows";
                if(showWarning) {
                    message += "\nWARNING: " +
                            "Reducing rows will reset board";
                }
                rows = Integer.parseInt(JOptionPane.showInputDialog(
                        null, message));
                if(rows <= 0) {
                    throw new IllegalArgumentException();
                }
            } catch(Exception e) {
                rows = 0;
                JOptionPane.showMessageDialog(
                        null, "Please enter a valid number of rows");
            }

            if(rows != 0) {break;}
        }

        return rows;
    }

    /******************************************************************
     * gets the desired number of columns based on JOptionPane user
     * input
     * @param showWarning whether to show an additional warning
     *                    about resetting the board
     * @return int number of desired columns from the user
     *****************************************************************/
    public int getNewNumberOfColumns(boolean showWarning) {
        int col;

        while(true) {
            try {
                String message = "Desired number of columns";
                if(showWarning) {
                    message += "\nWARNING: " +
                            "Reducing columns will reset board";
                }
                col = Integer.parseInt(JOptionPane.showInputDialog(
                        null, message));
                if(col <= 0) {
                    throw new IllegalArgumentException();
                }
            } catch(Exception e) {
                col = 0;
                JOptionPane.showMessageDialog(
                        null, "Please enter a valid number of columns");
            }

            if(col != 0) {break;}
        }

        return col;
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
                if (gameLogic.getStatus().equals(GameStatus.USER_WON)) {
                    JOptionPane.showMessageDialog(null, "You won!");
                    gameLogic.reset();
                    colors = new ArrayList<>();
                    updateBoard();
                } else if (gameLogic.getStatus().equals(GameStatus.USER_LOST)) {
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
                gameLogic.setWinningValue(getNewWinValue());
            } else if(e.getSource() == reset) {
                gameLogic.reset();
                colors = new ArrayList<>();
                updateBoard();
            } else if(e.getSource() == quit) {
                System.exit(1);
            } else if(e.getSource() == resizeBoardButton) {
                resetBoardWithInput(false, true);
            } else {
                //TODO probably don't need anything here but idk
            }
        }
    }
}