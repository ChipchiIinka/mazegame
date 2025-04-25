package edu.penzgtu.util;

import edu.penzgtu.model.Node;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Pathfinding {
    private static final int SIZE = 32;
    private static final int OBSTACLE_CYCLE = 4;

    public static List<Point> aStarPathfinding(char[][] maze) {
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

    public static boolean isObstacleFree(char[][] maze, int x, int y, int time) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return false;
        if (maze[y][x] != 'O') return maze[y][x] == '.' || maze[y][x] == 'G';
        return (time % OBSTACLE_CYCLE) < (OBSTACLE_CYCLE / 2);
    }

    public static char getCellContent(char[][] maze, int x, int y, int time) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return '#';
        if (maze[y][x] != 'O') return maze[y][x];
        return isObstacleFree(maze, x, y, time) ? '.' : 'O';
    }

    public static int getPathLengthWithTurns(List<Point> path) {
        if (path.size() < 2) return 0;
        int steps = path.size() - 1;
        int turns = 0;
        int currentDirection = 1;
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
}