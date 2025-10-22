package com.camoga.ant.ants.patterns;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Ant;
import com.camoga.ant.ants.Map;
import com.camoga.ant.ants.Map.Chunk;

public class PatternRaster extends Pattern {

    private byte[] raster;
    private int width;
    private int antx, anty;
    private int dirx, diry;

    public PatternRaster(byte[] raster, int width, int antx, int anty, int antdirx, int antdiry) {
        this.raster = raster;
        this.width = width;
        this.antx = antx;
        this.anty = anty;
        this.dirx = antdirx;
        this.diry = antdiry;
    }

    @Override
    public void buildPattern(AbstractAnt ant) { // FIX ant must be facing north or south to work properly
		if(ant.getType() != 0) throw new IllegalArgumentException("Invalid ant type");
        Ant a = (Ant) ant;
        int height = raster.length/width;
        for(int yc = 0; yc <= (height >> 7); yc++) {
            for(int xc = 0; xc <= (width >> 7); xc++) {
                Chunk chunk = ant.map.getChunk(xc, yc);
                for(int y = 0; y < Math.min(128,height-yc*128); y++) {
                    for(int x = 0; x < Math.min(128,width-xc*128); x++) {
                        chunk.cells[x+y*128] = raster[(x+xc*128) + (y+yc*128)*width];
                    }
                }
            }
        }
        a.xc = (antx&0xffffff80) >> 7;
        a.yc = (anty&0xffffff80) >> 7;
        a.x = antx&127;
        a.y = anty&127;
        a.diry = diry;
        a.dirx = dirx;
        ant.chunk = ant.map.getChunk(a.xc,a.yc);
    }
}
