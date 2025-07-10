package com.redtoast.simulation.base;

@Deprecated
public class Yeild extends RuntimeException{
    public Yeild(){
        super("yielded");
    }
    static Yeild yeild = new Yeild();
}
