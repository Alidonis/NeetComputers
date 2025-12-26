package com.redtoast.neet;

import com.redtoast.Computer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProcessManager extends Thread{
    //static functions
    private static final ArrayList<ProcessManager> processManagers = new ArrayList<>();
    private static final LinkedList<UUID> knownUUIDs = new LinkedList<>();

    //dynamic functions
    private boolean killFlag = false;
    private final LinkedBlockingQueue<Runnable> que = new LinkedBlockingQueue<>();

    private ProcessManager(){
        super();
    }

    @Override
    public void run(){
        do {
            try {
                que.take().run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (!killFlag);
    }

    public static void openNewThread(){
        ProcessManager processManager = new ProcessManager();
        processManager.start();
        processManagers.add(processManager);
    }

    public static void clear(){
        for (ProcessManager processManager : processManagers) processManager.killFlag = true;
        processManagers.clear();
        knownUUIDs.clear();
    }

    private static boolean donateTask(Runnable task) {
        Integer index = null;
        int score = 99999;
        for (int i = 0; i < processManagers.size(); i++) {
            if (!processManagers.get(i).killFlag && processManagers.get(i).que.size() < score) {
                index = i;
                score = processManagers.get(i).que.size();
            }
        }
        if (index==null || score == 99999) return false;
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
        return donateTask(() -> {
            if (computer.getRuntime() != null) {
                computer.getRuntime().tick();
                knownUUIDs.remove(computer.getUuid());
            }
        });
    }
}