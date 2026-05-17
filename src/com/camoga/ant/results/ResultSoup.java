package com.camoga.ant.results;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
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
import com.camoga.ant.strategies.StrategyInterface;

public class ResultSoup extends Result {
	protected long rule;
	protected int[] seed;
	protected int seedindex = 0;
	protected int offset = 0;
	protected int maxsoups;
	protected int patternSize;
	
	protected MultiKeyMap<Long,Integer[]> highwayfreq = new MultiKeyMap<>();
	protected int soupcount = 0;
	protected long maxiterations = 0; // max iterations per simulation
	protected long totaliterations = 0;

	protected HashSet<Integer> wip = new HashSet<>();

	protected int NUM_EXAMPLES = 10;

	/**
	 * 
	 * @param type rule type (0: 2d, 1: hex, 2: 3d, 3: 4d, 4: 45º)
	 * @param rule
	 * @param seed 12 byte seed as a int[4] with last byte reserved for the index
	 * @param patternSize square radius of random pattern (1: 3x3, 2: 5x5, etc)
	 */
	public ResultSoup(int type, long rule, int[] seed, int patternSize, long maxiterations, int maxsoups) {
		super(type);
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

	protected ResultSoup() {}

	// public ResultSoup(String filename) {
	// 	try {
	// 		this.loadData(filename);
	// 		this.print();
	// 	} catch(Exception e) {
	// 		e.printStackTrace();
	// 	}
	// }

	public synchronized void insertResult(ResultSet result, int seedindex) {
		totaliterations += result.iterations;
		soupcount++;
		
		highwayfreq.compute(new MultiKey<Long>(result.getHighway()), (k,v) -> {
			if(v == null) {
				v = new Integer[NUM_EXAMPLES+1]; // TODO change to int
				v[0] = 0;
				result.setNew();
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

		wip.remove(seedindex);
		if((soupcount&1023) == 0 || soupcount == maxsoups) {
			try {
				saveData();
				LOG.info("Saving file...");
			} catch(Exception e) {
				e.printStackTrace();
			}
			if(soupcount == maxsoups) {
				sendResult();
				reset();
				startWorkers();
			}
		}
	}

	public synchronized int[] getSeedIndex() {
		if(seedindex-offset >= maxsoups) {
			return null;
		}
		int[] seed = Arrays.copyOf(this.seed, 4);
		seed[3] = seedindex++;
		wip.add(seed[3]);
		return seed;
	}

	@Override
	public synchronized void sendResult() {
		if(soupcount != maxsoups) return;
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

	protected void reset() {
		seedindex = 0;
		totaliterations = 0;
		highwayfreq.clear();
		soupcount = 0;
		seed = generateSeed();
		rule = strategy.next();
	}

	@Override
	public ResultSet initAnt(AbstractAnt ant) {
		int[] seed = getSeedIndex();
		if(seed == null) return null;
		ResultSet result = ant.run(this.rule, this.maxiterations, new PatternRandom(getPatternSize(), seed));
		this.insertResult(result, seed[3]);
		return result;
	}

	public void print() {
		List<Entry<MultiKey<? extends Long>, Integer[]>> list = new ArrayList<>(highwayfreq.entrySet());
		list.sort(Comparator.comparingInt(entry -> -entry.getValue()[0]));
		Entry<MultiKey<? extends Long>, Integer[]> largest = null;
		int printcount = 0;
		for(Entry<MultiKey<? extends Long>, Integer[]> entry : list) {
			if(	largest == null || 
				largest.getKey().getKey(0) < entry.getKey().getKey(0) || 
				largest.getKey().getKey(0) == entry.getKey().getKey(0) && largest.getValue()[0] < entry.getValue()[0]
			)
				largest = entry;
			if(printcount++ < 10) LOG.info(entry.getKey().toString() + ": \t" + Arrays.toString(entry.getValue()));
		}
		if(largest != null) LOG.info("Largest highway: " + largest.getKey().toString() + ": \t" + Arrays.toString(largest.getValue()));
		LOG.info("Results for rule " + this.rule + " soups");
		LOG.info("Seed: " + this.getSeedString() + " ("+Arrays.toString(seed)+")");
		LOG.info("# of soups: " + this.soupcount);
		LOG.info("# of distinct patterns: " + this.highwayfreq.size());
		LOG.info("Distribution of patterns: ");
		LOG.info("# of iterations: " + this.totaliterations);
		LOG.info("Avg # of iterations: " + this.totaliterations/this.soupcount);
	}

	private static int[] generateSeed() {
		Random rand = new Random();
		int[] numseed = new int[4];
		for(int i = 0; i < 3; i++) 
			numseed[i] = rand.nextInt(916132832); // 62**5
		return numseed;
	}

	public int getType() { return type; }
	public long getRule() { return rule; }
	// public int[] getSeed() { return seed; }
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

	protected void saveData() throws IOException, Exception {
		DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("saves/"+getSeedString()+".langton", false)));
		os.writeByte(0); // Tipo de trabajo: 0 sopas, 1 noseq (es una prueba mela pela)
		os.write(type);
		os.writeLong(rule);
		os.writeInt(seed[0]);
		os.writeInt(seed[1]);
		os.writeInt(seed[2]);
		os.writeInt(seedindex);
		os.writeInt(offset);
		os.writeInt(maxsoups);
		os.write(patternSize);
		os.writeInt(soupcount);
		os.writeLong(maxiterations);
		os.writeLong(totaliterations);
		os.write(NUM_EXAMPLES);
		os.write(wip.size());
		for(int j: wip) {
			os.writeInt(j);
		}
		os.writeInt(highwayfreq.size());
		for(Entry<MultiKey<? extends Long>, Integer[]> entry: highwayfreq.entrySet()) {
			os.writeShort(entry.getKey().size());
			for(Long h: entry.getKey().getKeys()) {
				os.writeLong(h);
			}
			for(Integer i: entry.getValue()) {
				if(i == null) break;
				os.writeInt(i);
			}
		}

		os.close();
	}

	@Override
	protected StrategyInterface defaultStrategy() { // TODO use strategy to start a new result soup once it finishes
		return () -> rule;
	}
}
