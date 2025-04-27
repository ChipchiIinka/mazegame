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
    private static final int CYCLE_LENGTH = 4; // yellow x2, orange, red

    private final List<Obstacle> obstacles = new ArrayList<>();

    public char[][] generateValidMaze() {
        Random rand = new Random();
        int attempt = 0;
        while (true) {
            attempt++;
            char[][] maze = generateMaze(rand);
            List<Point> path = Pathfinding.aStarPathfinding(maze);
            if (!isValidMaze(path)) continue;

            char[][] mazeWithObs = addDynamicObstacles(maze, rand);
            path = Pathfinding.aStarPathfinding(mazeWithObs);
            if (isValidMaze(path)) return mazeWithObs;

            if (attempt >= MAX_ATTEMPTS) { rand = new Random(); attempt = 0; }
        }
    }

    private char[][] generateMaze(Random rand) {
        char[][] maze = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) for (int j = 0; j < SIZE; j++) maze[i][j] = '#';
        Stack<Point> stack = new Stack<>();
        maze[1][1] = '.'; stack.push(new Point(1, 1));
        int[][] dirs = {{0,2},{2,0},{0,-2},{-2,0}};
        while (!stack.isEmpty()) {
            Point cur = stack.peek();
            List<int[]> shuffled = new ArrayList<>(Arrays.asList(dirs));
            Collections.shuffle(shuffled, rand);
            boolean moved = false;
            for (int[] d : shuffled) {
                int nx = cur.x + d[0], ny = cur.y + d[1];
                if (nx > 0 && nx < SIZE-1 && ny > 0 && ny < SIZE-1 && maze[ny][nx] == '#') {
                    maze[ny][nx] = '.';
                    maze[cur.y + d[1]/2][cur.x + d[0]/2] = '.';
                    stack.push(new Point(nx, ny));
                    moved = true; break;
                }
            }
            if (!moved) stack.pop();
        }
        maze[1][1] = '.';
        maze[30][1] = 'G';
        return maze;
    }

    private char[][] addDynamicObstacles(char[][] base, Random rand) {
        char[][] maze = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) maze[i] = base[i].clone();
        obstacles.clear();
        int placed = 0, attempts = 0;
        while (placed < NUM_OBSTACLES && attempts < MAX_ATTEMPTS) {
            attempts++;
            int x = rand.nextInt(SIZE), y = rand.nextInt(SIZE);
            if (maze[y][x] != '.' || (x==1&&y==1) || maze[y][x]=='G') continue;
            if (isIntersection(maze, x, y)) continue;
            int[] dir = pickRandomDirection(rand);
            int offset = rand.nextInt(CYCLE_LENGTH);
            obstacles.add(new Obstacle(x, y, dir[0], dir[1], offset));
            maze[y][x] = 'O';
            placed++;
        }
        return maze;
    }

    private int[] pickRandomDirection(Random rand) {
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        return dirs[rand.nextInt(dirs.length)];
    }

    private boolean isIntersection(char[][] maze, int x, int y) {
        int cnt=0;
        if (y>0 && maze[y-1][x]=='.') cnt++;
        if (y<SIZE-1 && maze[y+1][x]=='.') cnt++;
        if (x>0 && maze[y][x-1]=='.') cnt++;
        if (x<SIZE-1 && maze[y][x+1]=='.') cnt++;
        return cnt!=2 || ((maze[y-1][x]=='.'&&maze[y][x-1]=='.')||
                (maze[y-1][x]=='.'&&maze[y][x+1]=='.')||
                (maze[y+1][x]=='.'&&maze[y][x-1]=='.')||
                (maze[y+1][x]=='.'&&maze[y][x+1]=='.'));
    }

    private boolean isValidMaze(List<Point> path) {
        if (path==null||path.size()<2) return false;
        int steps = Pathfinding.getPathLengthWithTurns(path);
        return steps>=MIN_STEPS && steps<=MAX_PATH_LENGTH;
    }

    public List<Obstacle> getObstacles() { return new ArrayList<>(obstacles); }
}
