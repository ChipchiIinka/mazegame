package edu.penzgtu.evolution;

public class Individual {
    private final Strategy strategy;
    private double fitness;
    public Individual(Strategy strat) { this.strategy = strat; }
    public Strategy getStrategy() { return strategy; }
    public double getFitness() { return fitness; }
    public void setFitness(double fitness) { this.fitness = fitness; }
}
