package com.camoga.ant.ants.patterns;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Ant;
import com.camoga.ant.ants.Map;
import com.camoga.ant.ants.Map.Chunk;

public class PatternRaster extends Pattern {

    private byte[] raster;
    private int width;
    private int antx, anty, antxc, antyc;
    private int dirx, diry;

    public PatternRaster(byte[] raster, int width, int antx, int anty, int antxc, int antyc, int antdirx, int antdiry) {
        this.raster = raster;
        this.width = width;
        this.antx = antx;
        this.anty = anty;
        this.dirx = antdirx;
        this.diry = antdiry;
        this.antxc = antxc;
        this.antyc = antyc;
    }

    @Override
    public void buildPattern(AbstractAnt ant) { // FIX ant must be facing north or south to work properly
		if(ant.getType() != 0) throw new IllegalArgumentException("Invalid ant type");
        Ant a = (Ant) ant;
        int height = raster.length/width;
        System.out.println("e");
        int i = 0;
        for(int yc = 0; yc <= (height >> 7); yc++) {
            for(int xc = 0; xc <= (width >> 7); xc++) {
                Chunk chunk = ant.map.getChunk(xc, yc);
                for(int y = 0; y < Math.min(128,height-yc*128); y++) {
                    for(int x = 0; x < Math.min(128,width-xc*128); x++) {
                        chunk.cells[x+y*128] = raster[(x+xc*128) + (y+yc*128)*width];
                        i++;
                    }
                }
            }
        }
        System.out.println(i);
        a.xc = antxc;
        a.yc = antyc;
        a.x = antx;
        a.y = anty;
        a.diry = diry;
        a.dirx = dirx;
        System.out.println(a.xc + ", " + a.yc + ", " + a.x + ", " + a.y + ", " + a.dirx + ", " + a.diry);
    }
}
