package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.ComputerSpecs;
import com.redtoast.Lua.LuaTranslater;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.APIS.ChipAPI;
import com.redtoast.simulation.APIS.FSAPI;
import com.redtoast.simulation.APIS.PaintAPI;
import com.redtoast.simulation.APIS.PeripheralsAPI;
import com.redtoast.simulation.LangAPI.LangAPI;
import com.redtoast.simulation.LangAPI.ValueTypes.Function;
import com.redtoast.simulation.peripheral.peripheralWrapper;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public abstract class Runtime {
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-luaVM");
    private static final LuaTranslater Lua = new LuaTranslater();
    private final LinkedList<LangAPI> APIS = new LinkedList<>();
    public Globals env;
    public LuaValue LuaDebug;
    public LuaValue LuaCoro;
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
        System.out.println("i hate my life");
        threads.add(thread);
        return thread.getUuid();
    }

    public Runtime(Computer Parent, FileHandler Files, int filePointer, int ROMPointer){
        parent = Parent;
        files = Files;
        addAPI(new FSAPI(this));
        addAPI(new PaintAPI(parent));
        addAPI(new PeripheralsAPI() {
            @Override
            public LinkedList<peripheralWrapper> getParentsPeripherals() {
                return getPeripherals();
            }
        });
        addAPI(new ChipAPI(parent, this, getSpecifications().MaxCores) {
            @Override
            public LinkedList<LangThread> getThreads() {
                return threads;
            }

            @Override
            public LangThread getThread() {
                return thread;
            }

            @Override
            public void addThread(LangThread t) {
                threads.add(t);
            }

            @Override
            public void registerEvent(String event, Function func) {
                eventTable.put(event, func);
            }
        });
        env = getGlobals();
        if (files.exists("rom/startup.lua")){
            MakeThread(files.readFile("rom/startup.lua"), "Lua 5.2");
        }else{
            debug.info("Entrypoint not found for {}, computer failed to start!", ROMPointer);
            kill=true;
        }
    }

    public abstract LinkedList<peripheralWrapper> getPeripherals();
    public abstract LinkedList<EventGeneric> getEvents();
    public abstract ComputerSpecs getSpecifications();

    public void addAPI(LangAPI api){
        APIS.add(api);
    }

    private Globals getGlobals(){
        Globals global = JsePlatform.debugGlobals();
        LuaDebug = global.get("debug");
        LuaCoro = global.get("coroutine");

        global.set("package",LuaValue.NIL);
        global.set("os",LuaValue.NIL);
        global.set("require",LuaValue.NIL);
        global.set("debug",LuaValue.NIL);
        global.set("io",LuaValue.NIL);
        global.set("file",LuaValue.NIL);
        global.set("load",LuaValue.NIL);
        global.set("luajava",LuaValue.NIL);
        global.set("dofile",LuaValue.NIL);
        global.set("loadfile",LuaValue.NIL);

        for (int x = 0; x < APIS.size(); x++){
            Lua.InductAPI(APIS.get(x), global);
        }

        return global;
    }

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
}