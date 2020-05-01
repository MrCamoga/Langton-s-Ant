package com.camoga.ant;

import static com.camoga.ant.Settings.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * File with rules is stored in the following way:
 * [offset]	[type]			[value]			[description]
 * 0000		64 bit integer	??				rule (e.g. RLRR = 13)
 * 0008		64 bit integer  ??				period of rule  (0 == no period found, 1 == period was being found but simulation got to max iterations and wasn't found (this includes ants that generate other patterns like squares or triangles))
 * 0016		64 bit integer
 * 0024		64 bit integer
 * .......
 *  
 * @author Carlos
 *
 */
public class IORules {
	
	public static void findErrors() {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			byte[] data = new byte[16];
			while(bis.available() > 0) {
				bis.read(data);
				ByteBuffer bb = ByteBuffer.wrap(data);
				long rule = bb.getLong();
				long period = bb.getLong();
				
				if(period == 1) {
					System.out.println("Rule needs to be tested again: " + rule);
				} else if(period%2==1) {
					System.out.println("Bad rule: " + rule + "; period: " + period);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static long[] getBadRules() {
		ArrayList<Long> rules = new ArrayList<Long>();
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			byte[] data = new byte[16];
			while(bis.available() > 0) {
				bis.read(data);
				ByteBuffer bb = ByteBuffer.wrap(data);
				long rule = bb.getLong();
				long period = bb.getLong();
				
				if(period%2==1) {
					rules.add(rule);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return rules.stream().mapToLong(l -> l).toArray();
	}
	
	public static void cleanRulesFile() {
		Map<Long, Long> rules = new HashMap<>();
		try {
			BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
			byte[] data = new byte[16];
			boolean duplicated = false;
			while(fis.available()>0) {
				fis.read(data);
				ByteBuffer bb = ByteBuffer.wrap(data);
				long rule = bb.getLong();
				long period = bb.getLong();
				if(period == 10 || period == 4 || period == 6 || period == 8 || period == 12 || period == 16 || period == 20) { //Rules with these periods are triangles/squares that have incorrectly been identified as highways
					System.out.println(rule +" removed");
					period = 1;
				}
				if(rules.get(rule)!= null && period != rules.get(rule)) {
					System.out.println("Duplicated: " + rule + ", new period: " + period + "; old period: " + rules.get(rule));
					duplicated = true;
				}
				rules.put(rule, period);
			}
			fis.close();
			
			if(duplicated) {
				System.out.println("Do you want to continue? y/n");
				String ans = new Scanner(System.in).nextLine();
				if(ans.equalsIgnoreCase("n")) {
					return;
				} else if(!ans.equalsIgnoreCase("y")) return;
			}
			
			ArrayList<Map.Entry<Long,Long>> list = new ArrayList<>(rules.entrySet());
			Collections.sort(list, (a,b) -> a.getKey().compareTo(b.getKey()));
			
			BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
			list.forEach(e -> {
				try {
					fos.write(ByteBuffer.allocate(16).putLong(e.getKey()).putLong(e.getValue()).array());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
			fos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Map.Entry<Long, Long>> getHighways() {
		HashMap<Long, Long> rules = new HashMap<Long, Long>();
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(Settings.file));
			while(bis.available() > 0) {
				ByteBuffer bb = ByteBuffer.wrap(bis.readNBytes(16));
				long rule = bb.getLong();
				long period = bb.getLong();
				if(period > 1) {
					rules.put(rule, period);
				}
			}
			bis.close();
			
			ArrayList<Map.Entry<Long, Long>> sorted = new ArrayList<Map.Entry<Long, Long>>(rules.entrySet());
			Collections.sort(sorted, (a,b) -> b.getValue().compareTo(a.getValue()));
			return sorted;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void getInfo() {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(Settings.file));
			HashMap<Long, Long> rules = new HashMap<Long, Long>();
			HashSet<Long> periods = new HashSet<Long>();
			HashSet<Long> ruleshighway = new HashSet<Long>();
			int numhighways = 0;
			int numunknown = 0;
			while(bis.available() > 0) {
				ByteBuffer bb = ByteBuffer.wrap(bis.readNBytes(16));
				long rule = bb.getLong();
				long period = bb.getLong();
				if(rule > 1<<20) continue;
				if(period != 0) {
					if(period==1) numunknown++;
					else {
						numhighways++;
						ruleshighway.add(rule);
					}
				}
				rules.put(rule, period);
				periods.add(period);
			}
	
			
			System.out.format("%d rules have been tested\n", rules.size());
			System.out.format("Of which %d form a highway\n", numhighways);
			System.out.format("Unknown rules: %d\n", numunknown);
			System.out.println(periods.size() + " distinct periods found");
			
			ArrayList<Map.Entry<Long, Long>> list = new ArrayList<Map.Entry<Long,Long>>(rules.entrySet());
			Collections.sort(list, (a,b)->Long.compareUnsigned(a.getKey(), b.getKey()));
			int allrules = 1;
			while(allrules < list.size() && allrules == list.get(allrules-1).getKey()) {allrules++;}
			System.out.format("All rules up to %d tested\n", allrules);
			int rulesize = (int)Math.floor(Math.log(allrules)/Math.log(2));
			System.out.format("All rules up to %d letters have been tested\n", rulesize);
			
			long search = Collections.binarySearch(list, new AbstractMap.SimpleEntry<Long, Long>(2L<<rulesize, 0L), (a,b) -> a.getKey().compareTo(b.getKey()));
			if(search < 0) search += (2<<rulesize) + 1;
			else search = (2<<rulesize) - search - 1;
			System.out.format("Rules of %d letters left to test: %d (%f%% complete)\n", rulesize+1, search, 100-100*search/(double)(2<<rulesize));
			System.out.println();
			
			Collections.sort(list, (a,b) -> Long.compareUnsigned(b.getValue(), a.getValue()));
			
//			long million = Collections.binarySearch(list, new AbstractMap.SimpleEntry<Long, Long>(0L, 1000000L), (a,b) -> a.getValue().compareTo(b.getValue()));
//			if(million < 0) million = million
//			System.out.format("Rules with period > 1e6: %d", million);
			
			System.out.println("\n============================\nTOP RULES WITH LONGEST PERIOD\n============================");
			System.out.format("|%-20s \t |%-15s \t |%-64s|\n", "Rule", "Period", "Rule String");
			for(int i = 0; i < 20; i++) {
				System.out.format("|%-20d \t |%-15d \t |%-64s|\n", list.get(i).getKey(), list.get(i).getValue(), Rule.string(list.get(i).getKey()));				
			}
			
			//See how many rules form highways that start in the same way
			int n = 9;
			HashMap<Integer, Integer> mods = new HashMap<Integer, Integer>();
			for(int i = 0; i < 1<<n; i++) mods.put(i, 0);
			for(long rule : ruleshighway) {
				if(true || (rule < (1<<19)) && rule >= (1<<n)) { //Only allow rules with at least "n" colors and below "allrules" to not count the rules found by selected search
					int index = (int) (rule&((1<<n)-1));
					mods.put(index, 1+mods.get(index));					
				}
			}
			
			ArrayList<Map.Entry<Integer, Integer>> modssorted = new ArrayList<Map.Entry<Integer,Integer>>(mods.entrySet());
			Collections.sort(modssorted, (a,b) -> b.getValue()-a.getValue());
			System.out.println("\nNum of rules that start with the same " + n + " letters");
			System.out.format("%5s  \t%5s\n", "start", "count");
//			modssorted.forEach(e -> System.out.format("%s : %4d\n", String.format("%-"+n+"s",Rule.string(e.getKey())).replace(" ", "L"), e.getValue()));
//			for(int i = 0; i < 100; i++) {
//				Entry<Integer, Integer> e = modssorted.get(i);
//				System.out.format("%s : %4d\n", String.format("%-"+n+"s",Rule.string(e.getKey())).replace(" ", "L"), e.getValue());
//			}
			
			//MOST COMMON PERIODS
			HashMap<Long, Integer> highwayperiodfreq = new HashMap<Long, Integer>();
			for(long rule : ruleshighway) {
				if(true || rule < allrules) {
					long period = rules.get(rule);
					highwayperiodfreq.put(period, 1+highwayperiodfreq.getOrDefault(period, 0));
				}
			}
			ArrayList<Map.Entry<Long, Integer>> freqsort = new ArrayList<Map.Entry<Long,Integer>>(highwayperiodfreq.entrySet());
			Collections.sort(freqsort, (a,b) -> b.getValue()-a.getValue());
			
			System.out.println("\nMost common periods: ");
			for(int i = 0; i < 20; i++) {
				Entry<Long, Integer> e = freqsort.get(i);
				System.out.println(e);
			}
			
//			int total = 0;
//			int formhighway = 0;
//			for(long rule : rules.keySet()) {
//				if(rule%8192 == 8106) {
//					if(rules.get(rule) != 1) {
//						total++;
//						if(rules.get(rule)!=0) formhighway++;
//					}
//				}
//			}
//			System.out.println(total);
//			System.out.println("Proportion: " + formhighway/(float)total);
			
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveRulesToTxt() {
		try {
			FileWriter fw = new FileWriter(new File("rulesperiods.txt"));
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			while(bis.available()>0) {
				ByteBuffer bb = ByteBuffer.wrap(bis.readNBytes(16));
				long rule = bb.getLong();
				long period = bb.getLong();
				String line = Rule.string(rule) + "\t " + period+"\n";
				fw.write(line);
			}
			bis.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * If Settings.file doesn't exist, a new empty file will be created
	 * @param highways if true only highways will be returned
	 * @return
	 */
	public static ArrayList<Long> searchSavedRules(boolean highways) {
		try {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
			} catch(FileNotFoundException e) {
				e.printStackTrace();
				FileOutputStream fos = new FileOutputStream(file);
				fos.close();
				bis = new BufferedInputStream(new FileInputStream(file));
			}
			ArrayList<Long> rules = new ArrayList<>();
			while(bis.available()>0) {
				ByteBuffer bb = ByteBuffer.wrap(bis.readNBytes(16));
				long rule = bb.getLong();
				long period = bb.getLong();
				if(highways) {
					if(period != 0 && period != 1) rules.add(rule);
				} else rules.add(rule);
			}
			rules.sort((a,b) -> a.compareTo(b));
			bis.close();
			System.err.println("Rules saved: " + rules.size());
			return rules;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}