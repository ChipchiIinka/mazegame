package edu.penzgtu;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MazeGame game = new MazeGame();
            game.setVisible(true);
        });
    }
}
