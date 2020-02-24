package com.camoga.ant.test;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;

import javax.imageio.ImageIO;

public class FindPeriod {
	public static void main(String[] args) throws Exception {
		highwayWidth2("RRLRLLRRLRRRRRRRRRLLLLRLRR.bin");
	}
	
	public static void highwayWidth(String path) throws Exception {
		BufferedImage image = ImageIO.read(new File(path));
		int width = image.getWidth(), height = image.getHeight();
		int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		HashSet<Integer> subperiods = new HashSet<Integer>(); //Subperiod is the period of one row
		boolean unknown = false;
		h:for(int y = 0; y < height; y++) {
			p:for(int period = 1; period < width-1000; period++) {
				for(int x = 0; x < width-period; x++) {
					if(pixels[x+y*width] != pixels[x+period+y*width]) continue p;
				}
				System.out.println(y + ": " + period);
				subperiods.add(period);
				continue h;
			}
			System.out.println(y + ": Unknown");
			unknown = true;
		}
		System.out.println("Subperiods: " + subperiods);
		long lcm = lcm(subperiods.stream().mapToInt(e -> e).toArray());
		if(!unknown) System.out.println("Period = " + lcm);
		else System.out.println("Period >= " + lcm);
	}
	
	public static void highwayWidth2(String path) throws Exception {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(path)));
		byte[] bw = new byte[4];
		bis.read(bw);
		int width = ByteBuffer.wrap(bw).getInt();
		byte[] pixels = bis.readAllBytes();
		bis.close();
		int height = pixels.length/width;
		System.out.println(width+ ","+height);
		HashSet<Integer> subperiods = new HashSet<Integer>(); //Subperiod is the period of one row
		boolean unknown = false;
		h:for(int y = 0; y < height; y++) {
			p:for(int period = 1; period < width-1000; period++) {
				for(int x = 0; x < width-period; x++) {
					if(pixels[x+y*width] != pixels[x+period+y*width]) continue p;
				}
				System.out.println(y + ": " + period);
				subperiods.add(period);
				continue h;
			}
			System.out.println(y + ": Unknown");
			unknown = true;
		}
		System.out.println("Subperiods: " + subperiods);
		long lcm = lcm(subperiods.stream().mapToInt(e -> e).toArray());
		if(!unknown) System.out.println("Period = " + lcm);
		else System.out.println("Period >= " + lcm);
	}
	
	public static long lcm(int[] l) {
		long result = l[0];
		for(int i = 1; i < l.length; i++) {
			result = result*l[i]/gcd(result,l[i]);
		}
		return result;
	}
	
	public static long gcd(long a, long b) {
		if(a==0) return b;
		return gcd(b%a,a);
	}
}
