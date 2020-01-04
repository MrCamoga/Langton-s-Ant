package com.camoga.ant;

import static com.camoga.ant.Settings.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * File with rules is stored in the following way:
 * [offset]	[type]			[value]			[description]
 * 0000		64 bit integer	??				rule (e.g. RRLR = 13)
 * 0008		64 bit integer  ??				period of rule  (0 == no period found, 1 == period was being found but simulation got to max iterations and wasn't found (this includes ants that generate other patterns like squares or triangles))
 * 0016		64 bit integer
 * 0024		64 bit integer
 * .......
 *  
 * @author Carlos
 *
 */
public class IORules {
	
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
				long cycle = bb.getLong();
				if(cycle == 10 || cycle == 4 || cycle == 6 || cycle == 8 || cycle == 12 || cycle == 16 || cycle == 20) { //Rules with these periods are triangles/squares that have incorrectly been identified as highways
					System.out.println(rule +" removed");
					cycle = 0;
				}
				if(rules.get(rule)!= null && cycle != rules.get(rule)) {
					System.out.println("Duplicated: " + rule + ", new cycle: " + cycle + "; old cycle: " + rules.get(rule));
					duplicated = true;
				}
				rules.put(rule, cycle);
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
			
			FileOutputStream fos = new FileOutputStream(file);
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
	
			
			
			System.out.println(rules.size() + " rules have been tested");
			System.out.println("Of which " + numhighways + " form a highway");
			System.out.println("Unknown rules: " + numunknown);
			System.out.println(periods.size() + " distinct periods found");
			
			
			
			
			ArrayList<Map.Entry<Long, Long>> list = new ArrayList<Map.Entry<Long,Long>>(rules.entrySet());
			Collections.sort(list, (a,b)->Long.compareUnsigned(a.getKey(), b.getKey()));
			int allrules = 1;
			while(allrules == list.get(allrules-1).getKey()) {allrules++;}
			System.out.println("All rules up to " + allrules + " tested");
			System.out.println("All rules of " + (int)Math.floor(Math.log(allrules)/Math.log(2)) +  " letters or less have been tested");
			
			System.out.println("\n============================\nTOP 10 RULES WITH LONGEST PERIOD\n============================");
			Collections.sort(list, (a,b) -> Long.compareUnsigned(b.getValue(), a.getValue()));
			System.out.format("%-20s \t %-10s \t %-64s\n", "Rule", "Period", "Rule String");
			for(int i = 0; i < 50; i++) {
				System.out.format("%-20d \t %-10d \t %-64s\n", list.get(i).getKey(), list.get(i).getValue(), Rule.string(list.get(i).getKey()));				
			}
			
			//See how many rules form highways that start in the same way
			int n = 5;
			HashMap<Integer, Integer> mods = new HashMap<Integer, Integer>();
			for(int i = 0; i < 1<<n; i++) mods.put(i, 0);
			for(long rule : ruleshighway) {
				if((rule < (1<<17)) && rule >= (1<<n)) { //Only allow rules with at least "n" colors and below "allrules" to not count the rules found by selected search
					int index = (int) (rule&((1<<n)-1));
					mods.put(index, 1+mods.get(index));					
				}
			}
			
			ArrayList<Map.Entry<Integer, Integer>> modssorted = new ArrayList<Map.Entry<Integer,Integer>>(mods.entrySet());
			Collections.sort(modssorted, (a,b) -> b.getValue()-a.getValue());
			System.out.println("\nNum of rules that start with the same " + n + " letters");
			System.out.format("%5s  \t%5s\n", "start", "count");
			modssorted.forEach(e -> System.out.format("%s : %4d\n", String.format("%-"+n+"s",Rule.string(e.getKey())).replace(" ", "L"), e.getValue()));
			
			//MOST COMMON PERIODS
			HashMap<Long, Integer> highwayperiodfreq = new HashMap<Long, Integer>();
			for(long rule : ruleshighway) {
				if(true || (rule < allrules)) {
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
			
//			long max = 0;
//			long rulemax = 0;
//			for(long rule : ruleshighway) {
//				if(rule%16384 == 15435) {
//					if(rules.get(rule) > max) {
//						max = rules.get(rule);
//						rulemax = rule;
//					}
//					System.out.println(rule + ": " + rules.get(rule));
//				}
//			}
//			System.out.println("Max: " + rulemax + "; " + max);
			
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveRulesToTxt() {
		String result = "RULE, PERIOD\n";
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			while(bis.available()>0) {
				ByteBuffer bb = ByteBuffer.wrap(bis.readNBytes(16));
				long rule = bb.getLong();
				long cycle = bb.getLong();
				result+=rule + ", " + cycle+"\n";
			}
			
			bis.close();
			FileWriter fw = new FileWriter(new File("rulescycles.txt"));
			fw.write(result);
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
	public static long[] searchSavedRules(boolean highways) {
		long[] savedRules = null;
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
			savedRules = rules.stream().mapToLong(i->i).toArray();
//			System.out.println(Arrays.toString(Arrays.copyOfRange(savedRules, savedRules.length-20, savedRules.length)));
			Arrays.sort(savedRules);
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("Rules saved: " + savedRules.length);
		return savedRules;
	}
}