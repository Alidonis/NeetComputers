package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.FS.FileHelper;
import com.redtoast.simulation.FS.FileSystem;
import com.redtoast.simulation.FS.Filepath;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.base.LanguageGeneric;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public abstract class Runtime {
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-luaVM");
    public final GlobalManager globalManager;
    public LangThread thread;
    private boolean kill = false;
    public FileSystem fs;
    public Computer parent;
    public Hashtable<String, Function> eventTable = new Hashtable<>();
    public LinkedList<LangThread> threads = new LinkedList<>();
    private boolean inTick = false;

    public Runtime(Computer Parent){
        globalManager = new GlobalManager();
        fs = Parent.getFs();
        parent = Parent;
    }

    /**
     * Creates the runtimes initial thread, automatically ran by computer parent class
     */
    public void load(){
        Filepath entryFile = fs.getFile(FileHelper.normalize(fs.build.entrypoint));
        if (entryFile.exists()){
            inTick=true;
            try{
                MakeThread(entryFile.readAll(), "Lua 5.2");
            }catch (Throwable e){
                debug.info("Computer encountered error at entrypoint: {}", e.toString());
                kill=true;
            }
            inTick=false;
        }else{
            debug.info("Entrypoint not found for computer, computer failed to start!");
            kill=true;
        }
    }

    /**
     * opens a new thread
     * @param script The code you are running
     * @param lang the name of the language your opening the scrip in (I.E Lua 5.2)
     * @return The UUID of the created thread
     */
    public UUID MakeThread(String script, String lang){
        if (!NeetComputers.hasLanguage(lang)){
            return null;
        }
        LanguageGeneric langObject = NeetComputers.getLanguage(lang);
        assert langObject != null;
        LangThread thread = langObject.createThread(script, this, parent, parent.getSpecifications());
        threads.add(thread);
        return thread.getUuid();
    }

    /**
     * gets an unordered list of all peripheral object
     * @return list of wrapped peripherals
     */
    public abstract LinkedList<Peripheral> getPeripherals();

    /**
     * retrieves a que of to-be-run events from its parent class
     * @return list of events
     */
    public abstract LinkedList<EventGeneric> getEvents();

    /**
     * ticks all contained threads forward once and perform maintenance tasks
     */
    public void tick(){
        if (!kill) {
            if (threads.isEmpty()) {
                kill = true;
                return;
            }
            LinkedList<Integer> deathQue = new LinkedList<>();
            for (int i = 0; i < threads.size(); i++) {
                if (threads.get(i).isAlive()) {
                    thread = threads.get(i);
                    inTick=true;
                    threads.get(i).tick();
                    inTick=false;
                    if (!threads.get(i).isAlive()) {
                        deathQue.add(i);
                    }
                } else {
                    deathQue.add(i);
                }
            }
            int tracker = 0;
            for (int i = 0; i < deathQue.size(); i++) {
                threads.remove(i-tracker);
                tracker++;
            }
            if (threads.isEmpty()) {
                kill = true;
            }
        }
    }

    /**
     * returns the state of the runtime
     * @return if the instance is dead or alive
     */
    public boolean isDead(){
        return kill;
    }

    /**
     * returns an unordered list of all threads, including rarely dead threads
     * @return list of threads
     */
    public LinkedList<LangThread> getThreads() {
        return threads;
    }

    /**
     * gets the tread that's currently being ticked or returns null
     * @return LangThread instance or null
     */
    public @Nullable LangThread getRunningThread() {
        if (!inTick) {return null;}
        return thread;
    }
}