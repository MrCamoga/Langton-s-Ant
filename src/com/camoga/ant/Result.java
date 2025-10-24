package com.camoga.ant;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.ResultSet;

public abstract class Result {
    
    public abstract void sendResult();

    public abstract ResultSet initAnt(AbstractAnt ant);
}
