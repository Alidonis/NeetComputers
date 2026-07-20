package com.redtoast.neet;

import com.redtoast.Computer;
import com.redtoast.simulation.value.Value;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class ProcessManager extends Thread{
    //static functions
    private static final ArrayList<ProcessManager> processManagers = new ArrayList<>();
    private static final LinkedList<UUID> knownUUIDs = new LinkedList<>();

    //Main thread functionality
    private static final Hashtable<UUID, Value> returnTable = new Hashtable<>();
    private record FunctionPackage(UUID uuuid, Runnable runnable){}
    private static final ArrayList<FunctionPackage> mainQue = new ArrayList<>();

    //dynamic functions
    private boolean killFlag = false;
    private boolean wrapUp = false;
    private final LinkedBlockingQueue<Runnable> que = new LinkedBlockingQueue<>();

    private native void attachC();
    private native void detachC();

    private ProcessManager(){
        super();
    }

    @Override
    public void run(){
        while (!killFlag) {
            try {
                que.take().run();
                if ((killFlag || wrapUp) && que.isEmpty()) break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void kill() {
        killFlag = true;
        que.add(() -> {});
        try{
            join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void wrapUp() {
        wrapUp = true;
        que.add(() -> {});
    }

    public boolean available(){
        return !(killFlag || wrapUp) && isAlive();
    }

    public static void openNewThread(){
        ProcessManager processManager = new ProcessManager();
        processManagers.add(processManager);
        processManager.start();
    }

    public static void clear(){
        for (ProcessManager processManager : processManagers) {
            processManager.kill();
        }
        processManagers.clear();
        knownUUIDs.clear();
    }

    private static boolean donateTask(Runnable task) {
        Integer index = null;
        int score = 99999;
        for (int i = 0; i < processManagers.size(); i++) {
            if (processManagers.get(i).available() && processManagers.get(i).que.size() < score) {
                index = i;
                score = processManagers.get(i).que.size();
            }
        }
        if (index==null || score == 99999) {
            return false;
        }
        processManagers.get(index).que.add(task);
        return true;
    }

    /**
     * Ques the base runtime of the given computer to be ticked, won't allow the computer to be added to the que if its already waiting
     * @param computer computer to queue
     * @return weather the que operation was successful, returns false if no threads available or computer already in que
     */
    public static boolean queComputerTick(Computer computer){
        if (knownUUIDs.contains(computer.getUuid())) return false;
        knownUUIDs.add(computer.getUuid());
        return donateTask(() -> {
            if (computer.getRuntime() != null) {
                computer.getRuntime().tick();
                knownUUIDs.remove(computer.getUuid());
            }
        });
    }
}