package edu.penzgtu.model;

public class Obstacle {
    public int x, y, dx, dy;

    public Obstacle(int x, int y, int dx, int dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}