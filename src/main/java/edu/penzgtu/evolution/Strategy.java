package edu.penzgtu.evolution;

import java.util.Arrays;
import java.util.Random;

public class Strategy {
    private final Action[] genes;
    public Strategy() {
        genes = new Action[4];
        Random rand = new Random();
        for (int i = 0; i < genes.length; i++) {
            genes[i] = Action.values()[rand.nextInt(Action.values().length)];
        }
    }
    public Strategy(Action[] genes) { this.genes = Arrays.copyOf(genes, genes.length); }
    public Action chooseAction(boolean frontFree, boolean rightFree) {
        int idx = (frontFree ? 0 : 2) + (rightFree ? 0 : 1);
        return genes[idx];
    }
    public Action[] getGenes() { return genes; }
    public Strategy copy() { return new Strategy(genes); }
}

