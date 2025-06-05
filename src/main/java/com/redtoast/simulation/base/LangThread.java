package com.redtoast.simulation.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public abstract class LangThread {
    private static Logger logger = LoggerFactory.getLogger("NeetComputers: Threads");
    private boolean killed = false;
    protected String errorMessage;
    private UUID uuid;
    public LangThread(){
        uuid = UUID.randomUUID();
    }
    public void log(String message){
        logger.info(message);
    }
    public void error(String message){
        logger.warn(message);
    }
    public void kill(String message){
        errorMessage = message;
        killed = true;
    }
    public void kill(){
        killed = true;
    }
    public UUID getUuid(){
        return uuid;
    }
    public boolean isAlive(){return !killed;}
    public abstract String getLand();
    public abstract void Yield();
    public abstract void Tick();
}