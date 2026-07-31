package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.simulation.FS.*;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.base.LanguageGeneric;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents the code execution of a computer, and ticks on the computer ticking thread
 */
public abstract class Runtime {
    //resources
    private final ComputerFileSystem fs;
    private final Computer parent;
    private LangThread thread = null;
    public APILoader loader = null;

    //state info
    private volatile boolean inTick = false;
    private boolean kill = false;

    private volatile boolean killFlag = false;

    public final Object tickLock = new Object();

    public Runtime(Computer Parent){
        fs = Parent.getFileSystem();
        parent = Parent;
    }

    public boolean isInTick() {
        return inTick;
    }

    public boolean isSafeToDrop() {
        synchronized (tickLock) {
            return !inTick;
        }
    }

    /**
        Fetch this runtime's file access
     */
    public ComputerFileSystem getFileSpace(){
        return fs;
    }

    /**
        Fetch the parent computer running this process
     */
    public Computer getParent() {
        return parent;
    }

    /**
     * Creates the runtimes initial thread, automatically ran by computer parent class
     */
    public void load(){
        DiskSystem diskSystem = fs.getHomeDisk();
        try{
            thread = diskSystem.getLanguage().createThread(diskSystem.getEntrypoint().readAll(), this, parent, parent.getConfiguration());
            if (!thread.isAlive()) {
                if (thread.getErrorMessage()==null) {
                    parent.stop();
                }else{
                    parent.crash(thread.getErrorMessage());
                }
            }
        }catch (Throwable e){
            if (e instanceof DiskError){
                parent.crash(e.getMessage());
            }else{
                APILoader.printJavaError(e);
                parent.crash("Unknown failure during boot (check logs)");
            }
            kill=true;
        }
    }

    public void requestKill(){
        killFlag = true;
    }

    public boolean shouldDie(){
        boolean temp = killFlag;
        if (temp) killFlag = false;
        return temp;
    }

    /**
     * ticks the process forward once and performs state maintenance
     */
    public void tick(){
        String errorMessage = null;
        synchronized (tickLock) {
            if (!kill) {
                if (thread == null) {
                    kill = true;
                    return;
                }
                if (thread.isAlive()) {
                    inTick = true;
                    try {
                        if (parent.isCrashed()) return;
                        thread.tick();
                    } finally {
                        inTick = false;
                    }
                    if (!thread.isAlive()) {
                        errorMessage = thread.getErrorMessage();
                        if (errorMessage != null) errorMessage = errorMessage.replaceFirst("\n\t\\[Java]: in \\?$", "").replaceAll("\t", "    ");
                        thread = null;
                    }
                }
                if (thread == null) {
                    kill = true;
                }
            }
        }
        if (shouldDie()){
            if (errorMessage==null) {
                parent.stop();
            }else{
                parent.crash(errorMessage);
            }
        } else if (errorMessage!=null) {
            parent.crash(errorMessage);
        }
    }

    /**
     * gets the source of the running thread for error readouts
     */
    public String getCurrentSource(){
        if (!inTick) return null;
        return thread.getSource();
    }

    /**
     * returns the state of the runtime
     * @return if the instance is dead or alive
     */
    public boolean isDead(){
        return kill;
    }

    /**
     * gets runtimes current thread, or null of thread if dead
     * @return LangThread instance or null
     */
    public @Nullable LangThread getThread() {
        return kill ? null : thread;
    }
}
