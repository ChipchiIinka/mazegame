package edu.penzgtu.evolution;

import edu.penzgtu.util.MazeGenerator;

import java.util.*;

public class EvolutionStrategy implements EvolutionaryAlgorithm {
    private List<Individual> parents;
    private final int mu = 5;
    private final Random rand = new Random();

    @Override
    public void initialize(int populationSize) {
        parents = new ArrayList<>();
        for (int i = 0; i < mu; i++) parents.add(new Individual(new Strategy()));
    }

    @Override
    public void evaluate(MazeGenerator generator) {
        for (Individual ind : parents) {
            ind.setFitness(FitnessEvaluator.evaluate(ind.getStrategy(), generator));
        }
    }

    @Override
    public void evolve() {
        List<Individual> offspring = new ArrayList<>();
        int lambda = 30;
        for (int i = 0; i < lambda; i++) {
            Strategy base = parents.get(rand.nextInt(mu)).getStrategy().copy();
            for (int j = 0; j < base.getGenes().length; j++) {
                if (rand.nextDouble() < 0.1) base.getGenes()[j] = Action.values()[rand.nextInt(Action.values().length)];
            }
            offspring.add(new Individual(base));
        }
        for (Individual child : offspring) child.setFitness(FitnessEvaluator.evaluate(child.getStrategy(), new MazeGenerator()));
        offspring.sort(Comparator.comparingDouble(Individual::getFitness).reversed());
        parents = new ArrayList<>(offspring.subList(0, mu));
    }

    @Override
    public Individual getBest() {
        return Collections.max(parents, Comparator.comparingDouble(Individual::getFitness));
    }
}
