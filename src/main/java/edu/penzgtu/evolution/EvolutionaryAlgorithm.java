package edu.penzgtu.evolution;

import edu.penzgtu.util.MazeGenerator;

public interface EvolutionaryAlgorithm {
    void initialize(int populationSize);
    void evaluate(MazeGenerator generator);
    void evolve();
    Individual getBest();
}
