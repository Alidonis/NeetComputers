package com.redtoast.simulation.FS;

public class DiskError extends Exception{
    public DiskError(String message){
        super(message);
    }
    public DiskError(Exception cause){
        super(cause.getMessage(), cause);
    }
}
