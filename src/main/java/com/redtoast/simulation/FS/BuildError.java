package com.redtoast.simulation.FS;

public class BuildError extends Exception{
    public BuildError(String message){
        super(message);
    }
    public BuildError(Exception cause){
        super(cause.getMessage(), cause);
    }
}
