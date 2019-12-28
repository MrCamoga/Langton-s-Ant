package com.camoga.ant;

import java.io.ByteArrayOutputStream;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.tools.ant.DirectoryScanner;

public class IORules {
	
	private static final String file = "test2.langton";
	
	public static void cleanRulesFile() {
		Map<Long, Integer> rules = new HashMap<>();
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[12];
			boolean duplicated = false;
			while(fis.available()>0) {
				fis.read(data);
				ByteBuffer bb = ByteBuffer.wrap(data);
				long rule = bb.getLong();
				int cycle = bb.getInt();
				if(cycle == 10 || cycle == 4 || cycle == 8 || cycle == 12 || cycle == 16 || cycle == 20) {
					System.out.println(rule +" removed");
					cycle = 0;
				}
				if(rules.get(rule)!= null && cycle != rules.get(rule)) {
					System.out.println("Duplicated: " + rule + ", " + cycle + "; " + rules.get(rule));
					duplicated = true;
				}
				rules.put(rule, cycle); // 851 has two cycles: 292 and 244
			}
			fis.close();
			
			if(duplicated) {
				System.out.println("Do you want to continue? y/n");
				String ans = new Scanner(System.in).nextLine();
				if(ans.equalsIgnoreCase("n")) {
					return;
				} else if(!ans.equalsIgnoreCase("y")) return;
			}
			
			ArrayList<Map.Entry<Long,Integer>> list = new ArrayList<>(rules.entrySet());
			Collections.sort(list, (a,b) -> a.getKey().compareTo(b.getKey()));
			
			LinkedHashMap<Long, Integer> sorted = new LinkedHashMap<>();
			list.forEach((e) -> sorted.put(e.getKey(), e.getValue()));
			
			FileOutputStream fos = new FileOutputStream(file);
			sorted.forEach((k,v) -> {
				try {
					fos.write(ByteBuffer.allocate(12).putLong(k).putInt(v).array());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
			fos.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void saveRulesToTxt() {
		String result = "RULE, PERIOD\n";
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[12];
			while(fis.available()>0) {
				fis.read(data, 0, 12);
				ByteBuffer bb = ByteBuffer.wrap(data);
				long rule = bb.getLong();
				int cycle = bb.getInt();
//				if(cycle != 0)
				result+=rule + ", " + cycle+"\n";
			}
			
			fis.close();
			FileWriter fw = new FileWriter(new File("rulescycles.txt"));
			fw.write(result);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static long[] searchSavedRules() {
		long[] savedRules = null;
		try {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch(FileNotFoundException e) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.close();
				fis = new FileInputStream(file);
			}
			ArrayList<Long> rules = new ArrayList<>();
			while(fis.available()>0) {
				byte[] data = new byte[12];
				fis.read(data);
				rules.add(ByteBuffer.wrap(data).getLong());
			}
			savedRules = rules.stream().mapToLong(i->i).toArray();
//			System.out.println(Arrays.toString(Arrays.copyOfRange(savedRules, savedRules.length-20, savedRules.length)));
			Arrays.sort(savedRules);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("Rules saved: " + savedRules.length);
		return savedRules;
	}
	
	public static long[] searchRules(int cycle) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(new File("C:\\Users\\usuario\\workspace\\CELLULAR AUTOMATA\\Langton-s-Ant\\"+cycle));
		scanner.setIncludes(new String[] {"*.png"});
		scanner.scan();
		String[] files = scanner.getIncludedFiles();
		long[] rules = Stream.of(files).mapToLong(s -> Long.parseLong(s.substring(0, s.length()-4))).toArray();
		return rules;
	}
}
