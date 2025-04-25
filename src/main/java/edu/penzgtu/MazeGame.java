package edu.penzgtu;

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
    private static final int MIN_STEPS = 70;
    private static final int MAX_PATH_LENGTH = 150;
    private static final int MAX_ATTEMPTS = 1000;
    private static final int NUM_OBSTACLES = 8;
    private static final int OBSTACLE_CYCLE = 4;
    private final char[][] maze;
    private final Agent agent;
    private final List<Obstacle> obstacles = new CopyOnWriteArrayList<>();
    private int stepCount;
    private int generation;
    private final JLabel stepLabel;
    private final JLabel goalLabel;
    private final JLabel generationLabel;
    private final Timer timer;
    private boolean isSimulationRunning;
    private final List<Point> pathHistory;
    private boolean justTurned;
    private int time;

    public MazeGame() {
        setTitle("Maze with Dynamic Obstacles");
        setSize(SIZE * CELL_SIZE + 50, SIZE * CELL_SIZE + 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        maze = generateValidMaze();

        agent = new Agent(30, 1, 1);
        stepCount = 0;
        generation = 0;
        time = 0;
        pathHistory = new ArrayList<>();
        justTurned = false;

        JPanel controlPanel = new JPanel();
        JButton stepButton = new JButton("Сделать шаг");
        JButton runButton = new JButton("Запустить/Остановить");
        stepLabel = new JLabel("Пройденных шагов: 0");
        goalLabel = new JLabel("Цель: Не достигнута");
        generationLabel = new JLabel("Поколение: " + generation);

        stepButton.addActionListener(e -> performStep());
        runButton.addActionListener(e -> toggleSimulation());

        controlPanel.add(stepButton);
        controlPanel.add(runButton);
        controlPanel.add(stepLabel);
        controlPanel.add(goalLabel);
        controlPanel.add(generationLabel);

        add(controlPanel, BorderLayout.SOUTH);
        add(new MazePanel(), BorderLayout.CENTER);

        timer = new Timer(100, e -> performStep());
    }

    private char[][] generateValidMaze() {
        Random rand = new Random(42);
        int attempt = 0;
        while (true) {
            attempt++;
            char[][] maze = generateMaze(rand);
            List<Point> pathWithoutObstacles = aStarPathfinding(maze);
            if (!isValidMaze(pathWithoutObstacles)) {
                System.out.println("Attempt " + attempt + ": Invalid maze without obstacles (no path or invalid length)");
                continue;
            }
            char[][] mazeWithObstacles = addDynamicObstacles(maze, rand);
            List<Point> path = aStarPathfinding(mazeWithObstacles);
            if (isValidMaze(path)) {
                System.out.println("Valid maze generated on attempt " + attempt + ", path length: " + getPathLengthWithTurns(path));
                return mazeWithObstacles;
            }
            System.out.println("Attempt " + attempt + ": Invalid maze with obstacles (no path or invalid length)");
            if (attempt >= MAX_ATTEMPTS) {
                System.out.println("Reached maximum attempts (" + MAX_ATTEMPTS + "), retrying with new seed");
                rand = new Random();
                attempt = 0;
            }
        }
    }

    private char[][] generateMaze(Random rand) {
        char[][] maze = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                maze[i][j] = '#';
            }
        }
        Stack<Point> stack = new Stack<>();
        maze[1][1] = '.';
        stack.push(new Point(1, 1));

        int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
        int maxDepth = 100;
        int depth = 0;
        while (!stack.isEmpty() && depth < maxDepth) {
            Point current = stack.peek();
            int x = current.x;
            int y = current.y;
            List<int[]> shuffledDirections = new ArrayList<>(Arrays.asList(directions));
            Collections.shuffle(shuffledDirections, rand);
            boolean moved = false;

            for (int[] dir : shuffledDirections) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx > 0 && nx < SIZE - 1 && ny > 0 && ny < SIZE - 1 && maze[ny][nx] == '#') {
                    maze[ny][nx] = '.';
                    maze[y + dir[1]/2][x + dir[0]/2] = '.';
                    stack.push(new Point(nx, ny));
                    moved = true;
                    depth++;
                    break;
                }
            }
            if (!moved) stack.pop();
        }

        maze[1][30] = '.';
        maze[30][1] = 'G';
        maze[1][29] = '.';
        maze[29][1] = '.';
        return maze;
    }

    private char[][] addDynamicObstacles(char[][] maze, Random rand) {
        char[][] mazeWithObstacles = new char[SIZE][SIZE];
        for (int i = 0; i < maze.length; i++) {
            mazeWithObstacles[i] = maze[i].clone();
        }
        List<Point> obstaclesList = new ArrayList<>();
        obstacles.clear();
        int attempts = 0;
        while (obstaclesList.size() < NUM_OBSTACLES && attempts < 100) {
            int x = rand.nextInt(SIZE);
            int y = rand.nextInt(SIZE);
            if (mazeWithObstacles[y][x] == '.' && !(x == 30 && y == 1) && !(x == 1 && y == 30)) {
                mazeWithObstacles[y][x] = 'O';
                List<Point> path = aStarPathfinding(mazeWithObstacles);
                if (isValidMaze(path)) {
                    obstaclesList.add(new Point(x, y));
                } else {
                    mazeWithObstacles[y][x] = '.';
                }
            }
            attempts++;
        }
        for (Point p : obstaclesList) {
            obstacles.add(new Obstacle(p.x, p.y, 0, 0));
        }
        return mazeWithObstacles;
    }

    private List<Point> aStarPathfinding(char[][] maze) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.fScore));
        Set<String> closed = new HashSet<>();
        Point start = new Point(30, 1);
        Point goal = new Point(1, 30);
        openSet.add(new Node(0, 0, start, new ArrayList<>()));

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            Point current = currentNode.pos;
            int time = currentNode.time;
            List<Point> path = currentNode.path;

            if (current.equals(goal)) {
                path.add(current);
                return path;
            }

            int timeCycle = time % OBSTACLE_CYCLE;
            String state = current.x + "," + current.y + "," + timeCycle;
            if (closed.contains(state)) continue;
            closed.add(state);

            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE && isObstacleFree(maze, nx, ny, time + 1)) {
                    List<Point> newPath = new ArrayList<>(path);
                    newPath.add(current);
                    int gScore = newPath.size();
                    int hScore = Math.abs(nx - goal.x) + Math.abs(ny - goal.y);
                    int fScore = gScore + hScore;
                    openSet.add(new Node(fScore, time + 1, new Point(nx, ny), newPath));
                }
            }
        }
        return null;
    }

    private boolean isObstacleFree(char[][] maze, int x, int y, int time) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return false;
        if (maze[y][x] != 'O') return maze[y][x] == '.' || maze[y][x] == 'G';
        return (time % OBSTACLE_CYCLE) < (OBSTACLE_CYCLE / 2);
    }

    private boolean isValidMaze(List<Point> path) {
        if (path == null || path.size() < 2) return false;
        int steps = getPathLengthWithTurns(path);
        return steps >= MIN_STEPS && steps <= MAX_PATH_LENGTH; // Updated to 150
    }

    private int getPathLengthWithTurns(List<Point> path) {
        if (path.size() < 2) return 0;
        int steps = path.size() - 1;
        int turns = 0;
        int currentDirection = 1; // Down
        for (int i = 1; i < path.size() - 1; i++) {
            Point curr = path.get(i);
            Point next = path.get(i + 1);
            int dx = next.x - curr.x;
            int dy = next.y - curr.y;
            int newDirection;
            if (dx == 1) newDirection = 0;
            else if (dx == -1) newDirection = 2;
            else if (dy == 1) newDirection = 1;
            else newDirection = 3;
            if (newDirection != currentDirection) turns++;
            currentDirection = newDirection;
        }
        return steps + turns;
    }

    private void performStep() {
        if (stepCount >= MAX_STEPS) {
            resetAgent();
            generation++;
            generationLabel.setText("Поколение: " + generation);
            stepCount = 0;
            time = 0;
            pathHistory.clear();
            stepLabel.setText("Пройденных шагов: " + stepCount);
            return;
        }

        if (isGoalReached()) {
            timer.stop();
            isSimulationRunning = false;
            goalLabel.setText("Цель: Достигнута");
            return;
        }

        Point frontCell = getFrontCell();
        char frontContent = getCellContent(frontCell.x, frontCell.y, time);

        if (frontContent == 'G') {
            moveForward();
            justTurned = false;
            completeStep();
            return;
        }
        if (frontContent == 'O') {
            turnLeft();
            justTurned = true;
            completeStep();
            return;
        }
        if (frontContent == '#') {
            turnRight();
            justTurned = true;
            frontCell = getFrontCell();
            frontContent = getCellContent(frontCell.x, frontCell.y, time);
            int maxTurns = 3;
            while (frontContent == '#' && maxTurns > 0) {
                turnLeft();
                frontCell = getFrontCell();
                frontContent = getCellContent(frontCell.x, frontCell.y, time);
                maxTurns--;
            }
            completeStep();
            return;
        }
        if (frontContent == '.') {
            moveForward();
            justTurned = false;
        }

        if (!justTurned) {
            Point rightCell = getRightCell();
            char rightContent = getCellContent(rightCell.x, rightCell.y, time);
            if (rightContent == '.' || rightContent == 'G') {
                turnRight();
                justTurned = true;
                completeStep();
                return;
            }
        }

        completeStep();
    }

    private Point getFrontCell() {
        int x = agent.x;
        int y = agent.y;
        return switch (agent.direction) {
            case 0 -> new Point(x, y - 1); // Up
            case 1 -> new Point(x + 1, y); // Right
            case 2 -> new Point(x, y + 1); // Down
            case 3 -> new Point(x - 1, y); // Left
            default -> new Point(x, y);
        };
    }

    private Point getRightCell() {
        int x = agent.x;
        int y = agent.y;
        return switch (agent.direction) {
            case 0 -> new Point(x + 1, y); // Up -> Right
            case 1 -> new Point(x, y + 1); // Right -> Down
            case 2 -> new Point(x - 1, y); // Down -> Left
            case 3 -> new Point(x, y - 1); // Left -> Up
            default -> new Point(x, y);
        };
    }

    private char getCellContent(int x, int y, int time) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return '#';
        if (maze[y][x] != 'O') return maze[y][x];
        return isObstacleFree(maze, x, y, time) ? '.' : 'O';
    }

    private void moveForward() {
        Point front = getFrontCell();
        if (front.x >= 0 && front.x < SIZE && front.y >= 0 && front.y < SIZE) {
            agent.x = front.x;
            agent.y = front.y;
        }
    }

    private void turnLeft() {
        agent.direction = (agent.direction + 3) % 4;
    }

    private void turnRight() {
        agent.direction = (agent.direction + 1) % 4;
    }

    private void completeStep() {
        pathHistory.add(new Point(agent.x, agent.y));
        stepCount++;
        time++;
        stepLabel.setText("Пройденных шагов: " + stepCount);
        repaint();
    }

    private boolean isGoalReached() {
        return maze[agent.y][agent.x] == 'G';
    }

    private void resetAgent() {
        agent.x = 30;
        agent.y = 1;
        agent.direction = 1; // Down
        justTurned = false;
    }

    private void toggleSimulation() {
        if (isSimulationRunning) {
            timer.stop();
        } else {
            timer.start();
        }
        isSimulationRunning = !isSimulationRunning;
    }

    class MazePanel extends JPanel {
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
                        g.setColor(isObstacleFree(maze, x, y, time) ? Color.ORANGE : Color.RED);
                        g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
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

    static class Agent {
        int x, y;
        int direction;

        Agent(int x, int y, int direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }
    }

    static class Obstacle {
        int x, y, dx, dy;

        Obstacle(int x, int y, int dx, int dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }
    }

    static class Node {
        int fScore, time;
        Point pos;
        List<Point> path;

        Node(int fScore, int time, Point pos, List<Point> path) {
            this.fScore = fScore;
            this.time = time;
            this.pos = pos;
            this.path = path;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MazeGame game = new MazeGame();
            game.setVisible(true);
        });
    }
}