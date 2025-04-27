package edu.penzgtu.evolution;

import edu.penzgtu.util.MazeGenerator;

import java.util.*;

public class GeneticAlgorithm implements EvolutionaryAlgorithm {
    private List<Individual> population;
    private int populationSize;
    private final Random rand = new Random();

    @Override
    public void initialize(int populationSize) {
        this.populationSize = populationSize;
        population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(new Individual(new Strategy()));
        }
    }

    @Override
    public void evaluate(MazeGenerator generator) {
        for (Individual ind : population) {
            double fit = FitnessEvaluator.evaluate(ind.getStrategy(), generator);
            ind.setFitness(fit);
        }
    }

    @Override
    public void evolve() {
        List<Individual> newPop = new ArrayList<>();
        while (newPop.size() < populationSize) {
            Individual a = tournament();
            Individual b = tournament();
            Strategy[] kids = crossover(a.getStrategy(), b.getStrategy());
            for (Strategy s : kids) {
                mutate(s);
                newPop.add(new Individual(s));
                if (newPop.size() >= populationSize) break;
            }
        }
        population = newPop;
    }

    @Override
    public Individual getBest() {
        return Collections.max(population, Comparator.comparingDouble(Individual::getFitness));
    }

    private Individual tournament() {
        Individual best = null;
        for (int i = 0; i < 3; i++) {
            Individual cand = population.get(rand.nextInt(populationSize));
            if (best == null || cand.getFitness() > best.getFitness()) best = cand;
        }
        return best;
    }

    private Strategy[] crossover(Strategy p1, Strategy p2) {
        Action[] g1 = p1.getGenes();
        Action[] g2 = p2.getGenes();
        int point = rand.nextInt(g1.length);
        Action[] c1 = new Action[g1.length];
        Action[] c2 = new Action[g1.length];
        for (int i = 0; i < g1.length; i++) {
            if (i < point) { c1[i] = g1[i]; c2[i] = g2[i]; }
            else { c1[i] = g2[i]; c2[i] = g1[i]; }
        }
        return new Strategy[]{ new Strategy(c1), new Strategy(c2) };
    }

    private void mutate(Strategy strat) {
        Action[] genes = strat.getGenes();
        for (int i = 0; i < genes.length; i++) {
            double mutationRate = 0.1;
            if (rand.nextDouble() < mutationRate) {
                genes[i] = Action.values()[rand.nextInt(Action.values().length)];
            }
        }
    }
}