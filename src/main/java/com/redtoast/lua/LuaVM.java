package com.redtoast.lua;

import com.redtoast.Computer;
import com.redtoast.lua.APIS.LuaBios;
import com.redtoast.lua.APIS.LuaFS;
import com.redtoast.lua.APIS.LuaPaint;
import com.redtoast.lua.APIS.LuaPeripherals;
import com.redtoast.lua.peripheral.peripheralWrapper;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.UUID;

public abstract class LuaVM {
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-luaVM");
    private static final Logger luaLogger = LoggerFactory.getLogger("NeetComputers:luaVM");
    private final LinkedList<LuaAPI> APIS = new LinkedList<>();
    public Globals env;
    public LuaValue LuaDebug;
    public LuaValue LuaCoro;
    public Thread thread;
    private int pointer;
    private int ROMpointer;
    public int maxthreads = 3;
    private boolean kill = false;
    private static final Logger errorLog = LoggerFactory.getLogger("NeetComputers:errors");
    public FileHandler files;
    public Computer parent;

    private static class clockIn extends ZeroArgFunction {
        private final LuaVM VM;
        public clockIn(LuaVM vm){
            super();
            VM = vm;
        }

        @Override
        public LuaValue call() {
            VM.thread.ticket--;
            VM.thread.yield();
            return LuaValue.NIL;
        }
    }

    public static class Thread{
        private LuaVM parent;
        private LuaThread coroutine;
        private LuaValue chunk;
        private boolean kill = false;
        private int pointer;
        private String source;
        public int sleepfor = 0;
        public long lastSlept;
        public short ticket = 0;
        public UUID uuid;
        public Thread(LuaVM parentVM, String script, int point, String name){
            try{
                uuid = UUID.randomUUID();
                parent = parentVM;
                source = name+'-'+System.currentTimeMillis()+parent.threads.size();
                chunk = parent.env.load(script, source);
                coroutine = new LuaThread(parent.env, chunk);
                parent.LuaDebug.get("sethook").invoke(new LuaValue[]{coroutine,new clockIn(parentVM),LuaValue.NIL,LuaValue.valueOf(1000)});
                pointer = point;
            } catch (Exception e) {
                debug.error("Script {} at {} failed to compile", name, point);
                debug.error(e.toString());
                kill=true;
            }
        }

        public void Kill(){
            kill = true;
        }

        private boolean step(){
            Varargs result = coroutine.resume(LuaValue.NIL);
            if (!result.arg1().toboolean()){
                kill = true;
                if (result.arg(2).isnil()){
                    return true;
                }else{
                    if (result.arg(2).toString().equals("cannot resume dead coroutine")){
                        luaLogger.info("Script {} at {} has ran to completion!", source, pointer);
                    }else {
                        errorLog.error("thrown by {} in {} {}", source, pointer, result.arg(2).toString());
                    }
                    return true;
                }
            }
            return false;
        }
        public void tick(){
            if (kill) return;
            ticket += 29;
            while (ticket>0){
                if (kill) return;
                step();
            }
        }
        public void yield(){parent.LuaCoro.get("yield").invoke(LuaValue.NIL);}
        public boolean isDead(){return kill;}
    }

    public LinkedList<Thread> threads = new LinkedList<>();

    public LuaVM(Computer Parent, FileHandler Files, int filePointer, int ROMPointer){
        parent = Parent;
        pointer = filePointer;
        ROMpointer = ROMPointer;
        files = Files;
        addAPI(new LuaFS(this));
        addAPI(new LuaPaint(parent));
        addAPI(new LuaPeripherals() {
            @Override
            public LinkedList<peripheralWrapper> getParentsPeripherals() {
                return getPeripherals();
            }
        });
        addAPI(new LuaBios(parent, this) {
            @Override
            public LinkedList<Thread> getThreads() {
                return threads;
            }

            @Override
            public Thread getThread() {
                return thread;
            }

            @Override
            public void addThread(Thread t) {
                threads.add(t);
            }
        });
        env = getGlobals();
        if (files.exists("rom/startup.lua")){
            threads.add(new Thread(this, files.readFile("rom/startup.lua"),ROMPointer,"startup.lua"));
        }else{
            debug.info("Entrypoint not found for {}, computer failed to start!", ROMPointer);
            kill=true;
        }
    }

    public abstract LinkedList<peripheralWrapper> getPeripherals();

    public void addAPI(LuaAPI api){
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
            APIS.get(x).insertSelf(global);
        }

        return global;
    }

    public void tick(){
        if (!kill) {
            if (threads.size()==0) {
                kill = true;
                return;
            }
            LinkedList<Integer> deathQue = new LinkedList<Integer>();
            for (int i = 0; i < threads.size(); i++) {
                if (!threads.get(i).isDead()) {
                    thread = threads.get(i);
                    threads.get(i).tick();
                    if (threads.get(i).isDead()) {
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