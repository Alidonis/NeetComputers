package com.redtoast.simulation.cache;

import java.util.LinkedList;

public class PackedFunctionCache {
    public String name;
    public LinkedList<StaticFunctionCache> functions;
    public PackedFunctionCache(String name, LinkedList<StaticFunctionCache> functions){
        this.name = name;
        this.functions = functions;
    }
}
