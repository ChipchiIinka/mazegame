package edu.penzgtu.view;

import edu.penzgtu.model.Agent;

import javax.swing.*;
import java.awt.*;

public class MazePanel extends JPanel {
    private static final int SIZE = 32;
    private static final int CELL_SIZE = 20;
    private static final int OBSTACLE_CYCLE = 4;
    private final char[][] maze;
    private final Agent agent;
    private int time;

    public MazePanel(char[][] maze, Agent agent) {
        this.maze = maze;
        this.agent = agent;
        this.time = 0;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (maze[y][x] == '#') {
                    g.setColor(Color.BLACK);
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else if (maze[y][x] == 'G') {
                    g.setColor(Color.GREEN);
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else if (maze[y][x] == 'O') {
                    int cycle = time % OBSTACLE_CYCLE;
                    if (cycle < 2) {
                        g.setColor(Color.ORANGE);
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    } else if (cycle == 2) {
                        g.setColor(Color.RED);
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    } else {
                        g.setColor(Color.RED);
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                        int centerX = x * CELL_SIZE + CELL_SIZE / 2;
                        int centerY = y * CELL_SIZE + CELL_SIZE / 2;
                        int[] xPoints = {
                                centerX, centerX + 6, centerX, centerX + 4, centerX, centerX - 4, centerX, centerX - 6
                        };
                        int[] yPoints = {
                                centerY - 6, centerY, centerY + 6, centerY + 4, centerY, centerY + 4, centerY - 6, centerY
                        };
                        g.fillPolygon(xPoints, yPoints, 8);
                    }
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        g.setColor(Color.BLUE);
        g.fillOval(agent.x * CELL_SIZE, agent.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.YELLOW);
        int cx = agent.x * CELL_SIZE + CELL_SIZE / 2;
        int cy = agent.y * CELL_SIZE + CELL_SIZE / 2;
        int dx = 0, dy = 0;
        switch (agent.direction) {
            case 0: dy = -CELL_SIZE / 2; break;
            case 1: dx = CELL_SIZE / 2; break;
            case 2: dy = CELL_SIZE / 2; break;
            case 3: dx = -CELL_SIZE / 2; break;
        }
        g.drawLine(cx, cy, cx + dx, cy + dy);
    }
}