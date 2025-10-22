package com.camoga.ant.ants;

import java.util.Arrays;
import java.util.function.LongPredicate;

public class ResultSet {
    public long period, dx, dy, winding;
    public long rule;
    public String ruleString;
    public long iterations;
    public int hash;
    public Long[] histogram;

    public ResultSet(AbstractRule rule, long iterations, int hash, long period) {
        this.rule = rule.getRule();
        this.period = period;
        this.iterations = iterations;
        this.hash = hash;
        this.ruleString = rule.ruleString;
        this.histogram = new Long[]{};
    }

    public ResultSet(AbstractRule rule, long iterations, int hash, long period, long dx, long dy, long winding, long[] histogram) {
        this(rule, iterations, hash, period);
        this.dx = dx;
        this.dy = dy;
        this.winding = winding;
        processHistogram(rule, histogram);
    }

    private void processHistogram(AbstractRule rule, long[] histogram) {
        int histsize = 0;
        while(histsize < histogram.length && histogram[histsize] != 0) {
            histogram[histsize] *= rule.turn[histsize]; // we store the ant direction in each state as the sign
            this.winding += histogram[histsize];
            histsize++;
        }
        this.winding >>= 2;
        this.histogram = Arrays.stream(histogram).limit(histsize).boxed().toArray(Long[]::new);
    }

    public Long[] getHighway() {
        Long[] highway = new Long[4+histogram.length];
        highway[0] = period;
        highway[1] = dx;
        highway[2] = dy;
        highway[3] = winding;
        System.arraycopy(histogram, 0, highway, 4, histogram.length);
        return highway;
    }

    public String toString() {
        return Long.toUnsignedString(rule) + "\t" + ruleString + "\t" + printHighway();
    }

    public String printHighway() {
        if(period == 0) return "";
        else if(period == 1) return "?";
        return Long.toUnsignedString(period) + ", " + dx + ", " + dy + ", " + winding + "\t" + Arrays.toString(histogram);
    }
}