package edu.penzgtu.evolution;

import edu.penzgtu.util.MazeGenerator;

import java.util.Random;

public class SimulatedAnnealing implements EvolutionaryAlgorithm {
    private Individual current;
    private double temperature;
    private double coolingRate;
    private final Random rand = new Random();

    @Override
    public void initialize(int populationSize) {
        current = new Individual(new Strategy());
        current.setFitness(FitnessEvaluator.evaluate(current.getStrategy(), new MazeGenerator()));
        temperature = 1000;
        coolingRate = 0.003;
    }

    @Override
    public void evaluate(MazeGenerator generator) {
    }

    @Override
    public void evolve() {
        Strategy candidateStrat = current.getStrategy().copy();
        for (int i = 0; i < candidateStrat.getGenes().length; i++) {
            if (rand.nextDouble() < 0.1) candidateStrat.getGenes()[i] = Action.values()[rand.nextInt(Action.values().length)];
        }
        Individual candidate = new Individual(candidateStrat);
        candidate.setFitness(FitnessEvaluator.evaluate(candidateStrat, new MazeGenerator()));
        double delta = candidate.getFitness() - current.getFitness();
        if (delta > 0 || Math.exp(delta / temperature) > rand.nextDouble()) {
            current = candidate;
        }
        temperature *= 1 - coolingRate;
    }

    @Override
    public Individual getBest() {
        return current;
    }
}
