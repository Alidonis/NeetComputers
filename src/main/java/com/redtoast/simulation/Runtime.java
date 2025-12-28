package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.simulation.FS.*;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.value.NVTable;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.base.LanguageGeneric;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;

public abstract class Runtime {
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:init-runtime");
    private final GlobalManager globalManager;
    private LangThread runningThread;
    private boolean kill = false;
    private final FileSpace fs;
    private final Computer parent;
    private LangThread thread = null;
    private boolean inTick = false;

    public interface FunctionCall {
        Value<?> call(FunctionInput parameters);
    }

    private FunctionCall que = null;
    private FunctionInput parameters = null;
    private Value<?> product = null;

    public Runtime(Computer Parent, NVTable NVRam){
        globalManager = new GlobalManager(this, NVRam);
        fs = Parent.getFs();
        parent = Parent;
    }

    public void queCall(FunctionCall call, FunctionInput parameters){
        if (que!=null || product!=null) return;
        que = call;
        this.parameters = parameters;
        product = null;
    }

    public boolean isInTick() {
        return inTick;
    }

    public GlobalManager getGlobals(){
        return globalManager;
    }

    public FileSpace getFileSpace(){
        return fs;
    }

    public Computer getParent() {
        return parent;
    }

    public Optional<Value<?>> pullQue(){
        if (product==null) {
            return Optional.empty();
        } else {
            Optional<Value<?>> buffer = Optional.of(product);
            product = null;
            return buffer;
        }
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
                MakeThread(bootPath.entryPoint().readAll(), bootPath.language().getVersion());
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

    private void MakeThread(String script, String lang){
        if (!NeetComputersServer.hasLanguage(lang)){
            return;
        }
        LanguageGeneric langObject = NeetComputersServer.getLanguage(lang);
        assert langObject != null;
        thread = langObject.createThread(script, this, parent, parent.getSpecifications());
    }

    /**
     * retrieves the event callbacks from the parent computer
     */
    public abstract LinkedList<EventGeneric.eventCallback> getCallbacks();

    public abstract boolean shouldDie();

    /**
     * ticks all contained threads forward once and perform maintenance tasks
     */
    public void tick(){
        if (!kill) {
            if (thread == null) {
                kill = true;
                return;
            }
            while (!parent.getEventQue().isEmpty()) {
                for (EventGeneric.eventCallback callback : getCallbacks()){
                    callback.onEvent(parent.getEventQue().getFirst());
                }
                parent.getEventQue().remove();
            }
            if (que!=null){
                product = que.call(parameters);
                que = null;
                parameters = null;
            }
            if (thread.isAlive()) {
                runningThread = thread;
                inTick=true;
                if (parent.isCrashed()) return;
                thread.tick();
                inTick=false;
                if (!thread.isAlive()) {
                    thread = null;
                }
            }
            if (thread == null) {
                kill = true;
            }
        }
        if (shouldDie()){
            parent.stop();
        }
    }

    /**
     * gets the source of the running thread for error readouts
     */
    public String getCurrentSource(){
        if (!inTick) return null;
        return runningThread.getSource();
    }

    /**
     * returns the state of the runtime
     * @return if the instance is dead or alive
     */
    public boolean isDead(){
        return kill;
    }

    /**
     * gets the tread that's currently being ticked or returns null
     * @return LangThread instance or null
     */
    public @Nullable LangThread getThread() {
        return thread;
    }
}