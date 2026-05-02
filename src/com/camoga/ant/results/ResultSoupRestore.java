package com.camoga.ant.results;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.collections4.keyvalue.MultiKey;

public class ResultSoupRestore extends ResultSoup {

	private ArrayList<Integer> wip2;

	public ResultSoupRestore(String filename) {
		try {
			loadData(filename);
		} catch(Exception e) {
			e.printStackTrace();
		}
		print();
	}

	/**
	 * Only save data once wip2 is empty to avoid missing rules on save
	 */
	@Override
	protected void saveData() throws IOException, Exception {
		if(wip2.size() == 0) super.saveData();
	}

	private void loadData(String filename) throws Exception {
		DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
		int worktype = is.read(); // TODO move to factory and instance this class if worktype is soup.
		type = is.read();
		rule = is.readLong();
		seed = new int[] {is.readInt(),is.readInt(),is.readInt(),0};
		seedindex = is.readInt();
		offset = is.readInt();
		maxsoups = is.readInt();
		patternSize = is.read();
		soupcount = is.readInt();
		maxiterations = is.readLong();
		totaliterations = is.readLong();
		NUM_EXAMPLES = is.read();
		int wipSize = is.read();
		wip2 = new ArrayList<>(wipSize);
		for(int i = 0 ; i < wipSize; i++) wip2.add(is.readInt());
		int numHighways = is.readInt();
		for(int i = 0; i < numHighways; i++) {
			int highwaySize = is.readShort();
			Long[] highway = new Long[highwaySize];
			for(int j = 0; j < highwaySize; j++) highway[j] = is.readLong();
			Integer[] examples = new Integer[NUM_EXAMPLES+1];
			examples[0] = is.readInt();
			for(int j = 0; j < examples[0] && ++j < examples.length;) {
				examples[j] = is.readInt();
			}
			highwayfreq.put(new MultiKey<>(highway), examples);
		}

		is.close();
	}

	public synchronized int[] getSeedIndex() {
		if(wip2.size() > 0) {
			int[] seed = Arrays.copyOf(this.seed, 4);
			seed[3] = wip2.get(wip2.size()-1);
			wip2.remove(wip2.size()-1);
			return seed;
		}
		return super.getSeedIndex();
	}
}
