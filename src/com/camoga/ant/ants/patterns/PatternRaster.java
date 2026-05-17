package com.camoga.ant.ants.patterns;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Ant;
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
		if(ant.getType() != 0) throw new RuntimeException("Only works for 2d ant");
        Ant a = (Ant) ant;
        int height = raster.length/width;
        for(int yc = 0; yc <= (height >> ant.cPOW); yc++) {
            for(int xc = 0; xc <= (width >> ant.cPOW); xc++) {
                Chunk chunk = ant.map.getChunk(xc, yc);
                for(int y = 0; y < Math.min(ant.cSIZE,height-yc*ant.cSIZE); y++) {
                    for(int x = 0; x < Math.min(ant.cSIZE,width-xc*ant.cSIZE); x++) {
                        chunk.cells[x+y*ant.cSIZE] = raster[(x+xc*ant.cSIZE) + (y+yc*ant.cSIZE)*width];
                    }
                }
            }
        }
        a.xc = (antx&-ant.cSIZE) >> ant.cPOW;
        a.yc = (anty&-ant.cSIZE) >> ant.cPOW;
        a.x = antx&ant.cSIZEm;
        a.y = anty&ant.cSIZEm;
        a.diry = diry;
        a.dirx = dirx;
        ant.chunk = ant.map.getChunk(a.xc,a.yc);
    }
}
