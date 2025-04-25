package edu.penzgtu;

import edu.penzgtu.model.Agent;
import edu.penzgtu.model.LeaderboardEntry;
import edu.penzgtu.model.Obstacle;
import edu.penzgtu.util.MazeGenerator;
import edu.penzgtu.util.Pathfinding;
import edu.penzgtu.view.ControlPanel;
import edu.penzgtu.view.MazePanel;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MazeGame extends JFrame {
    private static final int SIZE = 32;
    private static final int CELL_SIZE = 20;
    private static final int MAX_STEPS = 200;

    private static final int STATE_YELLOW = 0;
    private static final int STATE_ORANGE = 1;
    private static final int STATE_RED = 2;
    private static final int CYCLE_LENGTH = 4;

    private final char[][] maze;
    private final Agent agent;
    private final List<Obstacle> obstacles = new CopyOnWriteArrayList<>();

    private int stepCount = 0;
    private int generation = 1;
    private int wins = 0;
    private int time = 0;

    private final JLabel stepLabel;
    private final JLabel goalLabel;
    private final JLabel generationLabel;
    private final JTextArea leaderboardArea;
    private final List<LeaderboardEntry> leaderboard = new ArrayList<>();

    private final Timer timer;
    private boolean isSimulationRunning = false;

    private final List<Point> pathHistory = new ArrayList<>();
    private final Map<Integer, Set<Point>> deadEndRepeats = new HashMap<>();
    private final Set<Point> deathPoints = new HashSet<>();
    private boolean justTurned = false;
    private final Map<Point, Integer> deathByTimeoutPoints = new HashMap<>();
    private final Set<Point> forbiddenPoints = new HashSet<>();

    public MazeGame() {
        setTitle("Лабиринт с динамическими препятствиями");
        setSize(SIZE * CELL_SIZE + 50, SIZE * CELL_SIZE + 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        MazeGenerator gen = new MazeGenerator();
        maze = gen.generateValidMaze();
        if (maze == null) throw new RuntimeException("Не удалось сгенерировать валидный лабиринт");

        agent = new Agent(30, 1, 1);
        obstacles.addAll(gen.getObstacles());

        ControlPanel ctrl = new ControlPanel(this::performStep, this::toggleSimulation);
        stepLabel = ctrl.getStepLabel();
        goalLabel = ctrl.getGoalLabel();
        generationLabel = ctrl.getGenerationLabel();
        leaderboardArea = ctrl.getLeaderboardArea();

        add(ctrl, BorderLayout.SOUTH);
        add(new MazePanel(maze, agent), BorderLayout.CENTER);

        timer = new Timer(100, e -> performStep());

        updateWinPercentage();
    }

    private void performStep() {
        if (!isValidPosition(agent.x, agent.y)) {
            advanceGeneration();
            return;
        }

        int currState = getObstacleState(agent.x, agent.y, time);
        if (currState == STATE_RED) {
            deathPoints.add(new Point(agent.x, agent.y));
            advanceGeneration();
            return;
        }

        if (stepCount >= MAX_STEPS) {
            advanceGeneration();
            return;
        }

        if (isGoalReached()) {
            advanceGeneration(true);
            return;
        }

        Point here = new Point(agent.x, agent.y);
        if (!justTurned && (pathHistory.isEmpty() || !pathHistory.get(pathHistory.size() - 1).equals(here))) {
            pathHistory.add(here);
        }

        Point front = getFrontCell();
        Point right = getRightCell();

        if (isWallOrBlocked(front)) {
            if (isWallOrBlocked(right)) {
                int attempts = 0;
                while (isWallOrBlocked(getFrontCell()) && attempts < 4) {
                    turnLeft();
                    attempts++;
                }
                if (attempts >= 4) {
                    advanceGeneration();
                    return;
                }
            } else {
                turnRight();
            }
            justTurned = true;
            completeStep();
            return;
        }

        int frontState = getObstacleState(front.x, front.y, time);
        if (frontState != -1) {
            if (frontState == STATE_YELLOW) {
                agent.x = front.x;
                agent.y = front.y;
                justTurned = false;
            } else {
                justTurned = true;
            }
            completeStep();
            return;
        }

        if (shouldAvoid(front) || deathPoints.contains(front)) {
            turnRight();
            justTurned = true;
            completeStep();
            return;
        }

        char content = Pathfinding.getCellContent(maze, front.x, front.y, time);
        switch (content) {
            case 'G':
                agent.x = front.x;
                agent.y = front.y;
                justTurned = false;
                completeStep();
                break;
            case '.':
                agent.x = front.x;
                agent.y = front.y;
                justTurned = false;

                right = getRightCell();
                if (isValidPosition(right.x, right.y)) {
                    char rc = Pathfinding.getCellContent(maze, right.x, right.y, time);
                    if ((rc == '.' || rc == 'G') && !shouldAvoid(right) && !deathPoints.contains(right)) {
                        turnRight();
                        justTurned = true;
                    }
                }
                completeStep();
                break;
            default:
                completeStep();
        }
    }

    private boolean shouldAvoid(Point p) {
        if (forbiddenPoints.contains(p)) return true;
        for (Map.Entry<Integer, Set<Point>> e : deadEndRepeats.entrySet()) {
            if (e.getKey() >= generation) continue;
            if (e.getValue().contains(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWallOrBlocked(Point p) {
        if (!isValidPosition(p.x, p.y)) return true;
        char c = maze[p.y][p.x];
        return c == '#' || shouldAvoid(p) || deathPoints.contains(p);
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    private void advanceGeneration() {
        advanceGeneration(false);
    }

    private void advanceGeneration(boolean won) {
        if (!won && stepCount >= MAX_STEPS) {
            Point deathPoint = new Point(agent.x, agent.y);
            deathByTimeoutPoints.merge(deathPoint, 1, Integer::sum);
            if (deathByTimeoutPoints.get(deathPoint) >= 3) {
                addForbiddenArea(deathPoint);
            }
        }

        if (won) {
            wins++;
            boolean alreadyExists = leaderboard.stream()
                    .anyMatch(entry -> entry.steps() == stepCount);

            if (!alreadyExists) {
                leaderboard.add(new LeaderboardEntry(generation, stepCount));
                updateLeaderboard();
            }
        }

        recordDeadEnds();
        generation++;
        resetForNext();
        updateWinPercentage();
    }

    private void addForbiddenArea(Point center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int newX = center.x + dx;
                int newY = center.y + dy;
                if (isValidPosition(newX, newY)) {
                    forbiddenPoints.add(new Point(newX, newY));
                }
            }
        }
    }

    private void updateLeaderboard() {
        leaderboard.sort(Comparator.comparingInt(LeaderboardEntry::steps));
        StringBuilder sb = new StringBuilder();
        sb.append("Таблица лидеров:\n");
        for (LeaderboardEntry entry : leaderboard) {
            sb.append("Поколение: ").append(entry.generation()).append(" - Шаги: ").append(entry.steps()).append("\n");
        }
        leaderboardArea.setText(sb.toString());
    }


    private int getObstacleState(int x, int y, int t) {
        for (Obstacle o : obstacles) {
            if (o.getX() == x && o.getY() == y) {
                int cycle = t % CYCLE_LENGTH;
                if (cycle < 2) return STATE_YELLOW;
                if (cycle == 2) return STATE_ORANGE;
                return STATE_RED;
            }
        }
        return -1;
    }

    private void recordDeadEnds() {
        if (deadEndRepeats.containsKey(generation)) return;
        Set<Point> repeats = new HashSet<>();
        Map<Point, Integer> counts = new HashMap<>();

        for (Point p : pathHistory) {
            int count = counts.getOrDefault(p, 0) + 1;
            counts.put(p, count);
            if (count >= 2) {
                repeats.add(p);
            }
        }

        if (!repeats.isEmpty()) {
            deadEndRepeats.put(generation, repeats);
        }
    }

    private void resetForNext() {
        generationLabel.setText("Поколение: " + generation);
        stepCount = time = 0;
        stepLabel.setText("Пройденных шагов: 0");
        resetAgent();
        pathHistory.clear();
        justTurned = false;
        deathPoints.clear();
    }

    private void updateWinPercentage() {
        int completedGenerations = generation - 1;
        if (completedGenerations != 0) {
            double percentage = (double) wins / completedGenerations * 100.0;
            goalLabel.setText(String.format("Процент побед: %.2f%%", percentage));
        }
    }

    private Point getFrontCell() {
        return switch (agent.direction) {
            case 0 -> new Point(agent.x, agent.y - 1);
            case 1 -> new Point(agent.x + 1, agent.y);
            case 2 -> new Point(agent.x, agent.y + 1);
            case 3 -> new Point(agent.x - 1, agent.y);
            default -> new Point(agent.x, agent.y);
        };
    }

    private Point getRightCell() {
        return switch (agent.direction) {
            case 0 -> new Point(agent.x + 1, agent.y);
            case 1 -> new Point(agent.x, agent.y + 1);
            case 2 -> new Point(agent.x - 1, agent.y);
            case 3 -> new Point(agent.x, agent.y - 1);
            default -> new Point(agent.x, agent.y);
        };
    }

    private void turnLeft() {
        agent.direction = (agent.direction + 3) % 4;
    }

    private void turnRight() {
        agent.direction = (agent.direction + 1) % 4;
    }

    private void completeStep() {
        stepCount++;
        time++;
        stepLabel.setText("Пройденных шагов: " + stepCount);
        ((MazePanel) getContentPane().getComponent(1)).setTime(time);
        repaint();
    }

    private boolean isGoalReached() {
        return maze[agent.y][agent.x] == 'G';
    }

    private void resetAgent() {
        agent.x = 30;
        agent.y = 1;
        agent.direction = 1;
    }

    private void toggleSimulation() {
        if (isSimulationRunning) timer.stop();
        else timer.start();
        isSimulationRunning = !isSimulationRunning;
    }
}