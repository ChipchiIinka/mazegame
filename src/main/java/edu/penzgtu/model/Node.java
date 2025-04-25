package edu.penzgtu.model;

import java.awt.*;
import java.util.List;

public class Node {
    public int fScore, time;
    public Point pos;
    public List<Point> path;

    public Node(int fScore, int time, Point pos, List<Point> path) {
        this.fScore = fScore;
        this.time = time;
        this.pos = pos;
        this.path = path;
    }
}