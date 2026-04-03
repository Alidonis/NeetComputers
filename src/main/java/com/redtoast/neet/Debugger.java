package com.redtoast.neet;

import com.redtoast.simulation.APILoader;

import java.util.LinkedList;

/**
 * kindly never ever ever ever use this for literally anything, thanks!
 */
public class Debugger {
    @FunctionalInterface
    public interface Function {
        public Object main();
    }

    public static Object timeFunction(Function function, int milliThreshold, int tag){
        long time = System.currentTimeMillis();
        Object buffer = function.main();
        if (System.currentTimeMillis() - time >= milliThreshold) APILoader.profiler.warn("dubug function #{} took {} milliseconds to complete", tag, System.currentTimeMillis() - time);
        return buffer;
    }

    private static long time = 0;
    private record Timestamp(long time, int tag){}
    private static LinkedList<Timestamp> timeStamps = new LinkedList<>();

    public static void startWatch(){
        time = System.currentTimeMillis();
    }

    public static void timeStamp(int tag){
        timeStamps.add(new Timestamp(System.currentTimeMillis(), tag));
    }

    public static void dumpStamps(int threshold) {
        long currentTime = System.currentTimeMillis();
        if (currentTime-time >= threshold){
            APILoader.profiler.warn("dubug time stamps took {} milliseconds to complete", System.currentTimeMillis() - time);
            long backstamp = time;
            for (Timestamp timestamp : timeStamps) {
                APILoader.profiler.warn("> time stamp #{} took place {} milliseconds into process and {} milliseconds after last stamp", timestamp.tag, timestamp.time - time, timestamp.time - backstamp);
                backstamp = timestamp.time;
            }
            APILoader.profiler.warn("> process ended {} milliseconds after final stamp", currentTime - backstamp);
        }
        timeStamps.clear();
    }
}
