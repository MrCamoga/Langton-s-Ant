package com.camoga.ant.ants.patterns;

import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Map.Chunk;

public class PatternRandom extends Pattern {

    private int size;
    private int[] seed;

    /**
     * @param size radius of random pattern. Eg: 1 -> 3x3, 2-> 5x5,...
     * @param seed random seed to feed mersenne twister
     */
    public PatternRandom(int size, int[] seed) {
        this.size = size;
        this.seed = seed;
    }

    @Override
    public void buildPattern(AbstractAnt ant) {
        MersenneTwister mt = new MersenneTwister(seed);
        int rulesize = ant.getRule().getSize();
        // System.out.println(rulesize);
        for(int y = -size; y <= size; y++) {
            int ya = y & 127;
            int yc = y >> 7;
            for(int x = -size; x <= size; x++) {
                int xa = x & 127;
                int xc = x >> 7;
                Chunk c = ant.map.getChunk(xc,yc);
                c.cells[xa|(ya<<7)] = (byte)mt.nextInt(rulesize);
                // System.out.println(c.cells[xa|(ya<<7)]);
            }
        }
        // System.exit(0);
    }   
}