package com.redtoast.lua.APIS;

import com.redtoast.Computer;
import com.redtoast.lua.LuaAPI;
import com.redtoast.lua.LuaVM;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.LinkedList;
import java.util.UUID;

public abstract class LuaBios extends LuaAPI {
    Computer computer;
    LuaVM vm;
    int maxThreadCount = 6;

    public abstract LinkedList<LuaVM.Thread> getThreads();
    public abstract LuaVM.Thread getThread();
    public abstract void addThread(LuaVM.Thread thread);

    public LuaBios(Computer parent, LuaVM VM) {
        super("bios");
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
                LuaValue uuid = LuaValue.valueOf(computer.getUuid().toString());
                LuaTable metadata = new LuaTable();
                metadata.set("tag","uuid");
                metadata.set("hasUUID", LuaValue.TRUE);
                metadata.set("UUID_source", "computer");
                uuid.setmetatable(metadata);
                return uuid;
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });

        set("testUUID", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                LuaValue uuid = args[0];
                if (uuid.getmetatable()==null) return LuaValue.NIL;
                if (uuid.getmetatable().get("hasUUID").isnil()) return LuaValue.FALSE;
                return LuaValue.TRUE;
            }

            @Override
            public Rules getRules() {
                return new Rules("string");
            }
        });

        set("UUIDSource", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                LuaValue uuid = args[0];
                if (uuid.getmetatable()==null) return LuaValue.NIL;
                if (uuid.getmetatable().get("hasUUID").isnil()) return LuaValue.NIL;
                return LuaValue.valueOf(uuid.getmetatable().get("UUID_source").toString());
            }

            @Override
            public Rules getRules() {
                return new Rules("string");
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
                LuaTable metadata = new LuaTable();
                metadata.set("tag","uuid");
                metadata.set("hasUUID", LuaValue.TRUE);
                metadata.set("UUID_source", "thread");
                LuaValue uuidTagged = LuaValue.valueOf(uuid.toString());
                uuidTagged.setmetatable(metadata);
                addThread(thread);
                return uuidTagged;
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
                LuaTable metadata = new LuaTable();
                metadata.set("tag","uuid");
                metadata.set("hasUUID", LuaValue.TRUE);
                metadata.set("UUID_source", "thread");
                LuaValue uuidTagged = LuaValue.valueOf(uuid.toString());
                uuidTagged.setmetatable(metadata);
                return uuidTagged;
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
