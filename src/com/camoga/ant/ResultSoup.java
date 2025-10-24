package com.camoga.ant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.ResultSet;
import com.camoga.ant.ants.patterns.PatternRandom;
import com.camoga.ant.net.Client;
import static com.camoga.ant.Main.LOG;
import com.camoga.ant.net.packets.Packet06SoupResult;

public class ResultSoup extends Result {
    private int type;
    private long rule;
    private int[] seed;
	private int seedindex = 0;
	private int offset = 0;
	private int maxsoups;
	private int patternSize;
    
	private MultiKeyMap<Long,Integer[]> highwayfreq = new MultiKeyMap<>();
	private int soupcount = 0;
	private long maxiterations = 0; // max iterations per simulation
	private long totaliterations = 0;

	private int NUM_EXAMPLES = 10;

	/**
	 * 
	 * @param type rule type (0: 2d, 1: hex, 2: 3d, 3: 4d, 4: 45º)
	 * @param rule
	 * @param seed 12 byte seed as a int[4] with last byte reserved for the index
	 * @param patternSize square radius of random pattern (1: 3x3, 2: 5x5, etc)
	 */
    public ResultSoup(int type, long rule, int[] seed, int patternSize, long maxiterations, int maxsoups) {
        this.type = type;
        this.rule = rule;
        this.seed = seed != null ? seed:generateSeed();
		this.patternSize = patternSize;
		this.maxiterations = maxiterations;
		this.maxsoups = maxsoups;
    }

	public ResultSoup(int type, long rule, int[] seed, int patternSize, long maxiterations, int maxsoups, int seedindex) {
		this(type, rule, seed, patternSize, maxiterations, maxsoups);
		this.seedindex = seedindex;
		this.offset = seedindex;
	}

    public synchronized void insertResult(long iterations, int seedindex, Long ...highway) {
        totaliterations += iterations;
		soupcount++;
		
		highwayfreq.compute(new MultiKey<Long>(highway), (k,v) -> {
			if(v == null) {
				v = new Integer[NUM_EXAMPLES+1]; // TODO change to int
				v[0] = 0;
			}
			v[0]++;
			int insertIndex;
			if(v[0] <= NUM_EXAMPLES) insertIndex = v[0];
			else if(v[v.length-1] > seedindex) insertIndex = v.length-1;
			else return v;
			v[insertIndex] = seedindex;
			// Insertion sort
			for(int i = insertIndex-1; i >= 1 && v[i] > v[i+1]; i--) {
				int tmp = v[i+1];
				v[i+1] = v[i];
				v[i] = tmp;
			}
			return v;
		});

		if((soupcount&127) == 0) this.print();
		if(soupcount == maxsoups) sendResult();
    }

	public synchronized int[] getSeedIndex() {
		if(seedindex-offset >= maxsoups) return null;
		int[] seed = Arrays.copyOf(this.seed, 4);
		seed[3] = seedindex++;
		return seed;
	}

	@Override
	public synchronized void sendResult() {
		// if(System.currentTimeMillis()-lastResultsTime < DELAY_BETWEEN_RESULTS) return;
		try {
            if(highwayfreq.size() == 0)
				return;
			Packet06SoupResult packet = new Packet06SoupResult(this);
			Client.sendPacket(packet);
			LOG.info("Data sent to server");
		} catch(IOException e) {
			LOG.warning("Could not send rules to server");
		}
	}	

	@Override
	public ResultSet initAnt(AbstractAnt ant) {
		int[] seed = getSeedIndex();
		if(seed == null) return null;
		ResultSet result = ant.run(this.rule, this.maxiterations, new PatternRandom(getPatternSize(), seed));
		this.insertResult(result.iterations, seed[3], result.getHighway());
		return result;
	}

    public void print() {
		List<Entry<MultiKey<? extends Long>, Integer[]>> list = new ArrayList<>(highwayfreq.entrySet());
		list.sort(Comparator.comparingInt(entry -> -entry.getValue()[0]));
		System.out.println("Results for rule " + this.rule + " soups");
		System.out.println("# of soups: " + this.soupcount);
		System.out.println("# of distinct patterns: " + this.highwayfreq.size());
		System.out.println("Distribution of patterns: ");
		Entry<MultiKey<? extends Long>, Integer[]> largest = null;
        for(Entry<MultiKey<? extends Long>, Integer[]> entry : list) {
			if(largest == null || largest.getKey().getKey(0) < entry.getKey().getKey(0) || largest.getKey().getKey(0) == entry.getKey().getKey(0) && largest.getValue()[0] == entry.getValue()[0]) largest = entry;
            System.out.println(entry.getKey().toString() + ": \t" + Arrays.toString(entry.getValue()));
        }
		if(largest != null) System.out.println("Largest highway: " + largest.getKey().toString() + ": \t" + Arrays.toString(largest.getValue()));
		System.out.println("# of iterations: " + this.totaliterations);
		System.out.println("Avg # of iterations: " + this.totaliterations/this.soupcount);
    }

	private static int[] generateSeed() {
		Random rand = new Random();
		int[] numseed = new int[4];
		for(int i = 0; i < 3; i++) 
			numseed[i] = rand.nextInt(916132832);
		return numseed;
	}

	public int getType() { return type; }
	public long getRule() { return rule; }
	public int[] getSeed() { return seed; }
	public int getOffset() { return offset; }
	public int getCount() { return seedindex-offset; }
	public String getSeedString() {
		StringBuilder res = new StringBuilder(15);
		final char alphanumeric[] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
		for(int i = 0, x; i < 3; i++) {
			x = seed[i];
			for(int j = 0; j < 5; j++) {
				int q = x/62;
				int r = x-62*q;
				x = q;
				res.append(alphanumeric[r]);
			}
		}
		return res.toString();
	}
	public MultiKeyMap<Long,Integer[]> getHighways() { return highwayfreq; }
	public long getIterations() { return maxiterations; }
	public long getTotalIterations() { return totaliterations; }
	public int getPatternSize() { return patternSize; }
	// public int getChunkCheck() { return chunkCheck; }
}
