package edu.penzgtu.model;

public class Obstacle {
    public int x, y;
    public int dx, dy;
    public int cycleOffset;

    public Obstacle(int x, int y, int dx, int dy, int cycleOffset) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.cycleOffset = cycleOffset;
    }
}