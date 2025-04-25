package edu.penzgtu.util;

import edu.penzgtu.model.Obstacle;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MazeGenerator {
    private static final int SIZE = 32;
    private static final int MIN_STEPS = 70;
    private static final int MAX_PATH_LENGTH = 150;
    private static final int MAX_ATTEMPTS = 1000;
    private static final int NUM_OBSTACLES = 8;
    private static final int CYCLE_LENGTH = 4; // cycle: yellow x2, orange, red

    private final List<Obstacle> obstacles = new ArrayList<>();

    public char[][] generateValidMaze() {
        Random rand = new Random();
        int attempt = 0;
        while (true) {
            attempt++;
            char[][] maze = generateMaze(rand);
            List<Point> pathWithoutObstacles = Pathfinding.aStarPathfinding(maze);
            if (!isValidMaze(pathWithoutObstacles)) continue;

            char[][] mazeWithObstacles = addDynamicObstacles(maze, rand);
            List<Point> path = Pathfinding.aStarPathfinding(mazeWithObstacles);
            if (isValidMaze(path)) {
                return mazeWithObstacles;
            }
            if (attempt >= MAX_ATTEMPTS) {
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
                    maze[y + dir[1] / 2][x + dir[0] / 2] = '.';
                    stack.push(new Point(nx, ny));
                    moved = true;
                    depth++;
                    break;
                }
            }
            if (!moved) stack.pop();
        }

        maze[1][30] = '.';
        maze[30][1] = '.';
        maze[1][29] = '.';
        maze[30][1] = 'G';
        return maze;
    }

    private char[][] addDynamicObstacles(char[][] maze, Random rand) {
        char[][] mazeWithObstacles = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            mazeWithObstacles[i] = maze[i].clone();
        }
        obstacles.clear();
        List<Point> placed = new ArrayList<>();
        int attempts = 0;
        while (placed.size() < NUM_OBSTACLES && attempts < MAX_ATTEMPTS) {
            int x = rand.nextInt(SIZE);
            int y = rand.nextInt(SIZE);
            if (mazeWithObstacles[y][x] != '.' || (x == 30 && y == 1) || (x == 1 && y == 30)) {
                attempts++;
                continue;
            }
            Point p = new Point(x, y);
            if (isCornerOrIntersection(mazeWithObstacles, x, y)) {
                attempts++;
                continue;
            }
            boolean neighbor = false;
            for (Point q : placed) {
                if (Math.abs(q.x - x) + Math.abs(q.y - y) <= 1) {
                    neighbor = true;
                    break;
                }
            }
            if (neighbor) {
                attempts++;
                continue;
            }
            mazeWithObstacles[y][x] = 'O';
            List<Point> path = Pathfinding.aStarPathfinding(mazeWithObstacles);
            if (isValidMaze(path)) {
                int offset = rand.nextInt(CYCLE_LENGTH);
                obstacles.add(new Obstacle(x, y, offset, 0));
                placed.add(p);
            } else {
                mazeWithObstacles[y][x] = '.';
            }
            attempts++;
        }
        return mazeWithObstacles;
    }

    private boolean isCornerOrIntersection(char[][] maze, int x, int y) {
        boolean up = y > 0 && maze[y - 1][x] == '.';
        boolean down = y < SIZE - 1 && maze[y + 1][x] == '.';
        boolean left = x > 0 && maze[y][x - 1] == '.';
        boolean right = x < SIZE - 1 && maze[y][x + 1] == '.';
        int count = 0;
        if (up) count++;
        if (down) count++;
        if (left) count++;
        if (right) count++;
        if (count != 2) {
            return true;
        }
        return (up && left) || (up && right) || (down && left) || (down && right);
    }

    private boolean isValidMaze(List<Point> path) {
        if (path == null || path.size() < 2) return false;
        int steps = Pathfinding.getPathLengthWithTurns(path);
        return steps >= MIN_STEPS && steps <= MAX_PATH_LENGTH;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }
}
