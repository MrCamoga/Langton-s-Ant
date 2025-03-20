package com.camoga.ant.ants.patterns;

import java.util.Random;

import com.camoga.ant.ants.AbstractAnt;

public class PatternRandom extends Pattern {

    private int size;
    private long seed;

    public PatternRandom(int size, long seed) {
        this.size = size;
        this.seed = seed;
    }

    @Override
    public void buildPattern(AbstractAnt ant) {
        Random rnd = new Random(seed);
        throw new UnsupportedOperationException("Unimplemented method 'buildPattern'");
    }   
}