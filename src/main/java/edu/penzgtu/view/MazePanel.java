package edu.penzgtu.view;

import edu.penzgtu.evolution.Action;
import edu.penzgtu.evolution.Strategy;
import edu.penzgtu.model.Agent;
import edu.penzgtu.model.Obstacle;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MazePanel extends JPanel {
    private static final int SIZE = 32;
    private static final int CELL_SIZE = 20;
    private static final int CYCLE_LENGTH = 4;

    private char[][] maze;
    private final Agent agent;
    private Strategy strategy;
    private List<Obstacle> obstacles;
    private int steps;
    private boolean dead;
    private boolean goalReached;

    public MazePanel(char[][] maze) {
        this.maze = maze;
        this.agent = new Agent(1, 1, 1);
        setPreferredSize(new Dimension(SIZE * CELL_SIZE, SIZE * CELL_SIZE));
        resetSimulation();
    }

    public void setMaze(char[][] maze) {
        this.maze = maze;
        repaint();
    }

    public void setObstacles(List<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public void simulateStrategy(Strategy strat) {
        this.strategy = strat;
        resetSimulation();
        while (steps < 200 && !goalReached && !dead) {
            stepSimulation();
        }
        repaint();
    }

    public void stepStrategy(Strategy strat) {
        if (strategy != strat) {
            this.strategy = strat;
            resetSimulation();
        }
        if (steps < 200 && !goalReached && !dead) {
            stepSimulation();
        }
        repaint();
    }

    public void resetSimulation() {
        agent.x = 1;
        agent.y = 1;
        agent.direction = 1;
        steps = 0;
        dead = false;
        goalReached = false;
        repaint();
    }

    private void stepSimulation() {
        if (obstacles != null) updateObstacles();
        boolean frontFree = isFree(agent.direction);
        boolean rightFree = isFree((agent.direction + 1) % 4);
        Action action = strategy.chooseAction(frontFree, rightFree);

        switch (action) {
            case FORWARD:
                if (isFree(agent.direction)) moveAgent();
                break;
            case TURN_LEFT:
                agent.direction = (agent.direction + 3) % 4;
                break;
            case TURN_RIGHT:
                agent.direction = (agent.direction + 1) % 4;
                break;
            case STOP:
                break;
        }

        if (isGoalReached()) {
            goalReached = true;
        }

        if (obstacles != null) {
            for (Obstacle ob : obstacles) {
                if (ob.x == agent.x && ob.y == agent.y && ob.cycleOffset == CYCLE_LENGTH - 1) {
                    dead = true;
                    break;
                }
            }
        }

        steps++;
    }

    private void moveAgent() {
        switch (agent.direction) {
            case 0: agent.y--; break;
            case 1: agent.x++; break;
            case 2: agent.y++; break;
            case 3: agent.x--; break;
        }
    }

    public int getSteps() { return steps; }
    public boolean isDead() { return dead; }
    public boolean isGoalReached() { return maze[agent.y][agent.x] == 'G'; }

    private void updateObstacles() {
        for (Obstacle ob : obstacles) {
            int nx = ob.x + ob.dx;
            int ny = ob.y + ob.dy;
            if (nx <= 0 || nx >= SIZE-1 || ny <= 0 || ny >= SIZE-1 || maze[ny][nx] == '#') {
                ob.dx = -ob.dx;
                ob.dy = -ob.dy;
            } else {
                ob.x = nx;
                ob.y = ny;
            }
            ob.cycleOffset = (ob.cycleOffset + 1) % CYCLE_LENGTH;
        }
    }

    private boolean isFree(int dir) {
        int nx = agent.x, ny = agent.y;
        switch (dir) {
            case 0: ny--; break;
            case 1: nx++; break;
            case 2: ny++; break;
            case 3: nx--; break;
        }
        return ny >= 0 && ny < SIZE && nx >= 0 && nx < SIZE && maze[ny][nx] != '#';
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (maze == null || maze.length != SIZE) return;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                char cell = maze[y][x];
                int px = x * CELL_SIZE;
                int py = y * CELL_SIZE;
                switch (cell) {
                    case '#': g.setColor(Color.BLACK); break;
                    case 'G': g.setColor(Color.GREEN); break;
                    default: g.setColor(Color.WHITE);
                }
                g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            }
        }

        if (obstacles != null) {
            for (Obstacle ob : obstacles) {
                int px = ob.x * CELL_SIZE;
                int py = ob.y * CELL_SIZE;
                int c = ob.cycleOffset;
                if (c < 2) g.setColor(Color.YELLOW);
                else if (c == 2) g.setColor(Color.ORANGE);
                else g.setColor(Color.RED);
                g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            }
        }

        g.setColor(Color.BLUE);
        int ax = agent.x * CELL_SIZE;
        int ay = agent.y * CELL_SIZE;
        g.fillOval(ax, ay, CELL_SIZE, CELL_SIZE);

        g.setColor(Color.YELLOW);
        int cx = ax + CELL_SIZE / 2, cy = ay + CELL_SIZE / 2;
        int dx = 0, dy = 0;
        switch (agent.direction) {
            case 0: dy = -CELL_SIZE / 2; break;
            case 1: dx = CELL_SIZE / 2; break;
            case 2: dy = CELL_SIZE / 2; break;
            case 3: dx = -CELL_SIZE / 2; break;
        }
        g.drawLine(cx, cy, cx + dx, cy + dy);

        g.setColor(Color.BLACK);
        g.drawString("Шагов: " + steps, 5, SIZE * CELL_SIZE - 5);

        if (dead) {
            g.setColor(Color.RED);
            g.drawString("Статус: Агент погиб", 5, SIZE * CELL_SIZE - 25);
        } else if (isGoalReached()) {
            g.setColor(Color.GREEN);
            g.drawString("Статус: Успех!", 5, SIZE * CELL_SIZE - 25);
        }
    }
}