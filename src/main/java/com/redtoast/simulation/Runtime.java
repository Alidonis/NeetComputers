package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.simulation.FS.*;
import com.redtoast.simulation.value.NVTable;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.base.LanguageGeneric;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * represents the code execution of a computer, and ticks on the computer ticking thread
 */
public abstract class Runtime {
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:init-runtime");

    //resources
    private final GlobalManager globalManager;
    private final FileSpace fs;
    private final Computer parent;
    private LangThread thread = null;

    //state info
    private boolean inTick = false;
    private boolean kill = false;

    public Runtime(Computer Parent, NVTable NVRam){
        globalManager = new GlobalManager(this, NVRam);
        fs = Parent.getFs();
        parent = Parent;
    }

    /**
        Tests if the thread is being processed
     */
    public boolean isInTick() {
        return inTick;
    }

    /**
        Fetch this runtime's global space
     */
    public GlobalManager getGlobals(){
        return globalManager;
    }

    /**
        Fetch this runtime's file access
     */
    public FileSpace getFileSpace(){
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
        assert fs instanceof BootableFilespace;
        BootableFilespace bootableFilespace = (BootableFilespace) fs;
        try {
            boolean canBoot = bootableFilespace.canBoot();
            if (!canBoot){
                debug.warn("Computer refused to boot");
                kill=true;
            }
            BootableFilespace.BootPath bootPath = bootableFilespace.fetchBootPath();
            try{
                if (!NeetComputersServer.hasLanguage(bootPath.language().getVersion())){
                    return;
                }
                LanguageGeneric langObject = NeetComputersServer.getLanguage(bootPath.language().getVersion());
                assert langObject != null;
                thread = langObject.createThread(bootPath.entryPoint().readAll(), this, parent, parent.getConfiguration());
            }catch (Throwable e){
                APILoader.printJavaError(e);
                kill=true;
            }
        }catch (IOException ioException){
            debug.warn("Computer failed to boot ({})", ioException.getMessage());
            kill=true;
        }
        System.out.println("booting complete");
    }

    /**
     * called when runtime completes a tick, if true kills the process
     * @return if the process should die
     */
    public abstract boolean shouldDie();

    /**
     * ticks the process forward once and performs state maintenance
     */
    public void tick(){
        String errorMessage = null;
        if (!kill) {
            if (thread == null) {
                kill = true;
                return;
            }
            if (thread.isAlive()) {
                inTick=true;
                if (parent.isCrashed()) return;
                parent.getEventManager().update();
                thread.tick();
                inTick=false;
                if (!thread.isAlive()) {
                    errorMessage = thread.getErrorMessage();
                    thread = null;
                }
            }
            if (thread == null) {
                kill = true;
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