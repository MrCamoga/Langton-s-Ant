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
        // CARE histogram is null
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
            histogram[histsize] *= rule.turn[histsize]; // we store the ant direction for each state as the sign of the number.
            histsize++;
        }
        this.histogram = Arrays.stream(histogram).limit(histsize).boxed().toArray(Long[]::new);
    }

    public String toString() {
        return Long.toUnsignedString(rule) + "\t" + ruleString + "\t" + 
            (period > 1 ? (Long.toUnsignedString(period) + ", " + dx + ", " + dy + ", " + winding + "\t" + Arrays.toString(histogram)):(period == 1 ? "?":""));
    }
}