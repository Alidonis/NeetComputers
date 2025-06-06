package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.ComputerSpecs;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.base.LanguageGeneric;
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
    public FileHandler files;
    public Computer parent;
    public Hashtable<String, Function> eventTable = new Hashtable<>();
    public LinkedList<LangThread> threads = new LinkedList<>();

    public UUID MakeThread(String script, String lang){
        if (!NeetComputers.hasLanguage(lang)){
            return null;
        }
        LanguageGeneric langObject = NeetComputers.getLanguage(lang);
        assert langObject != null;
        LangThread thread = langObject.createThread(script, this, parent, getSpecifications());
        threads.add(thread);
        return thread.getUuid();
    }

    public Runtime(Computer Parent, FileHandler Files){
        parent = Parent;
        files = Files;
        globalManager = new GlobalManager();
        new APILoader(this, parent);
        if (files.exists("rom/startup.lua")){
            MakeThread(files.readFile("rom/startup.lua"), "Lua 5.2");
        }else{
            debug.info("Entrypoint not found for computer, computer failed to start!");
            kill=true;
        }
    }

    public abstract LinkedList<Peripheral> getPeripherals();
    public abstract LinkedList<EventGeneric> getEvents();
    public abstract ComputerSpecs getSpecifications();

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
                    threads.get(i).Tick();
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

    public boolean isDead(){
        return kill;
    }

    public LinkedList<Peripheral> getParentsPeripherals() {
        return getPeripherals();
    }

    public LinkedList<LangThread> getThreads() {
        return threads;
    }

    public LangThread getThread() {
        return thread;
    }

    public void registerEvent(String event, Function func) {
        eventTable.put(event, func);
    }
}