package com.redtoast.lua.APIS;

import com.redtoast.Computer;
import com.redtoast.lua.LuaAPI;
import com.redtoast.lua.LuaFunction;
import com.redtoast.lua.LuaVM;
import com.redtoast.lua.Rules;
import org.luaj.vm2.LuaValue;

import java.util.LinkedList;
import java.util.UUID;

public abstract class LuaChip extends LuaAPI {
    Computer computer;
    LuaVM vm;

    public abstract LinkedList<LuaVM.Thread> getThreads();
    public abstract LuaVM.Thread getThread();
    public abstract void addThread(LuaVM.Thread thread);

    public LuaChip(Computer parent, LuaVM VM, int maxThreadCount) {
        super("chip");
        computer = parent;
        vm = VM;

        set("getTime", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                return LuaValue.valueOf(System.currentTimeMillis());
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });

        set("getUUID", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                return LuaValue.valueOf(computer.getUuid().toString());
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });

        set("getThreadCount", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                return LuaValue.valueOf(getThreads().size());
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });

        set("getMaxThreadCount", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                return LuaValue.valueOf(maxThreadCount);
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });

        set("createThread", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                if (getThreads().size()>=maxThreadCount) return LuaValue.error("Thread cap for this machine reached, cant make more threads");
                LuaValue text = args[0];
                LuaVM.Thread thread = new LuaVM.Thread(vm,text.toString(),0,"null");
                UUID uuid = thread.uuid;
                addThread(thread);
                return LuaValue.valueOf(uuid.toString());
            }

            @Override
            public Rules getRules() {
                return new Rules("string");
            }
        });

        set("getCurrentThread", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                UUID uuid = getThread().uuid;
                return LuaValue.valueOf(uuid.toString());
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });

        set("killThread", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                LinkedList<LuaVM.Thread> threads = getThreads();
                for (LuaVM.Thread thread : threads){
                    if (thread.uuid.toString().equals(args[0].toString())){
                        thread.Kill();
                        return LuaValue.TRUE;
                    }
                }
                return LuaValue.FALSE;
            }

            @Override
            public Rules getRules() {
                return new Rules("string");
            }
        });



        set("shutdown", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                computer.Stop();
                return LuaValue.NIL;
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });

        set("version", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                return LuaValue.valueOf("NeetComputers beta 0.1");
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });
    }
}
