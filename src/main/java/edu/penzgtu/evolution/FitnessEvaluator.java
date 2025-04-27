package edu.penzgtu.evolution;

import edu.penzgtu.model.Agent;
import edu.penzgtu.util.MazeGenerator;

public class FitnessEvaluator {
    public static double evaluate(Strategy strat, MazeGenerator generator) {
        char[][] maze = generator.generateValidMaze();
        Agent agent = new Agent(1, 1, 1); // start facing RIGHT
        int maxSteps = 200;
        int steps;
        for (steps = 0; steps < maxSteps; steps++) {
            if (maze[agent.y][agent.x] == 'G') break;
            boolean frontFree = isFree(agent, maze, agent.direction);
            boolean rightFree = isFree(agent, maze, (agent.direction + 1) % 4);
            Action action = strat.chooseAction(frontFree, rightFree);
            switch (action) {
                case FORWARD:
                    if (frontFree) moveForward(agent);
                    break;
                case TURN_LEFT:
                    agent.direction = (agent.direction + 3) % 4;
                    break;
                case TURN_RIGHT:
                    agent.direction = (agent.direction + 1) % 4;
                    break;
                case STOP:
                    steps = maxSteps;
                    break;
            }
        }
        if (maze[agent.y][agent.x] == 'G') {
            return 1000 - steps;
        } else {
            int dist = Math.abs(agent.x - 30) + Math.abs(agent.y - 1);
            return 100 - dist;
        }
    }

    private static boolean isFree(Agent a, char[][] maze, int dir) {
        int nx = a.x, ny = a.y;
        switch (dir) {
            case 0: ny--; break;
            case 1: nx++; break;
            case 2: ny++; break;
            case 3: nx--; break;
        }
        return ny >= 0 && ny < maze.length && nx >= 0 && nx < maze[0].length && maze[ny][nx] != '#';
    }

    private static void moveForward(Agent a) {
        switch (a.direction) {
            case 0: a.y--; break;
            case 1: a.x++; break;
            case 2: a.y++; break;
            case 3: a.x--; break;
        }
    }
}