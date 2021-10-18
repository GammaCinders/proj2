package proj2;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GUI1024 {
    public static void main(String arg[]){

        //Setup for each element and the elements they contain
        JMenuBar topBar = new JMenuBar();
        JMenu options = new JMenu("Options");
        JMenuItem changeWinValue = new JMenuItem("Change Win Value");
        JMenuItem reset = new JMenuItem("Reset Game");
        JMenuItem quit = new JMenuItem("Quit");

        JButton resizeBoard = new JButton("Click to change board size");

        //Adding each element to the respective spot for the menu
        options.add(changeWinValue);
        options.add(reset);
        options.add(quit);
        topBar.add(options);
        topBar.add(resizeBoard);

        resizeBoard.setFocusable(false);

        JFrame gui = new JFrame ("Welcome to 1024!");
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GUI1024Panel2 panel = new GUI1024Panel2(changeWinValue, reset, quit, resizeBoard);
        panel.setFocusable(true);
        gui.getContentPane().add(panel);

        panel.resetBoardWithInput(true);

        gui.setSize(panel.getSize());
        gui.setJMenuBar(topBar);
        gui.pack();
        gui.setVisible(true);
    }
}


