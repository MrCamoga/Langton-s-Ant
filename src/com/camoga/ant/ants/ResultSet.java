package com.camoga.ant.ants;

import java.util.Arrays;

/**
 * Stores result of computation: rule, highway information, iterations, hash,...
 */
public class ResultSet {
    public long period, dx, dy, winding;
    public long rule;
    public String ruleString;
    public long iterations;
    public int hash;
    public Long[] histogram;

    private boolean newResult = false;

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
        boolean wrong = false;
        while(histsize < histogram.length && histogram[histsize] != 0) {
            if(histogram[histsize] < histogram[histsize+1]) {
                wrong = true;
                break;
            }
            // we store the ant direction in each state as the sign
            // TODO only do this in 2d
            histogram[histsize] *= rule.turn[histsize];
            this.winding += histogram[histsize];
            histsize++;
        }
        if((winding&3) != 0) wrong = true;
        if(!wrong) {
            this.winding >>= 2;
            this.histogram = Arrays.stream(histogram).limit(histsize).boxed().toArray(Long[]::new);
        } else {
            this.period = 1;
            this.dx = 0;
            this.dy = 0;
            this.winding = 0;
            this.histogram = new Long[]{};
        }
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

    public Long[] getSmallHighway() {
        return new Long[] {period,dx,dy,winding};
    }

    public String toString() {
        return Long.toUnsignedString(rule) + "\t" + ruleString + "\t" + printHighway();
    }

    public String printHighway() {
        if(period == 0) return "";
        else if(period == 1) return "?";
        return Long.toUnsignedString(period) + ", " + dx + ", " + dy + ", " + winding + "\t" + Arrays.toString(histogram);
    }

    // For logging purposes (e.g. only log new highways)
    public void setNew() {
        newResult = true;
    }

    public boolean isNew() {
        return newResult;
    }
}