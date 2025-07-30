package com.redtoast.simulation.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public abstract class LangThread {
    private static Logger logger = LoggerFactory.getLogger("NeetComputers: Threads");
    private int javaLag = 0;
    private long javaLagCache = 0;
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
    public abstract String getLang();
    public abstract void yield();
    public abstract void tick();
    public abstract String getSource();
    public abstract void crash(String message);
    public void taxJavaLag(short lagTime){
        javaLag += lagTime;
        javaLagCache += lagTime;
        if (javaLagCache>=5000){
            crash("Computer forcibly terminated due to lag, check console for more information");
        }
    }
    public int getJavaTaxBulk(short batch){
        int i = javaLag / batch;
        javaLag %= batch;
        return i;
    }
    public void clearJavaLag(){
        javaLag=0;
        javaLagCache=0;
    }
}