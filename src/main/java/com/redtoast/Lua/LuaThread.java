package com.redtoast.Lua;

import com.redtoast.Computer;
import com.redtoast.ComputerSpecs;
import com.redtoast.simulation.LangThread;
import com.redtoast.simulation.Runtime;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ZeroArgFunction;

public class LuaThread extends LangThread {
    private static class clockIn extends ZeroArgFunction {
        private final LuaThread parent;
        public clockIn(LuaThread parentThread){
            super();
            parent = parentThread;
        }

        @Override
        public LuaValue call() {
            parent.ticket--;
            parent.Yield();
            return LuaValue.NIL;
        }
    }

    private ComputerSpecs specs;
    private org.luaj.vm2.LuaThread coroutine;
    private LuaValue LuaCoro;
    public short ticket = 0;

    public LuaThread(String script, Runtime parentRuntime, Computer parentComputer, ComputerSpecs specification){
        super();
        try{
            specs = specification;
            LuaValue chunk = parentRuntime.env.load(script, "LuaThread");
            coroutine = new org.luaj.vm2.LuaThread(parentRuntime.env, chunk);
            parentRuntime.LuaDebug.get("sethook").invoke(new LuaValue[]{coroutine,new clockIn(this),LuaValue.NIL,LuaValue.valueOf(specification.BatchSize)});
            LuaCoro = parentRuntime.LuaCoro;
        } catch (Exception e) {
            kill("Lua failed to compile");
            error(e.toString());
            kill();
        }
    }

    @Override
    public void Yield() {
        LuaCoro.get("yield").invoke(LuaValue.NIL);
    }

    private void step(){
        Varargs result = coroutine.resume(LuaValue.NIL);
        if (!result.arg1().toboolean()){
            if (result.arg(2).toString().equals("cannot resume dead coroutine")) {
                log("LuaThread has ran to completion!");
            } else {
                kill(result.arg(2).toString());
                error("LuaThread threw " + result.arg(2).toString());
            }
            kill();
        }
    }

    @Override
    public void Tick(){
        if (!isAlive()) return;
        ticket += (short) specs.Batches;
        while (ticket>0) {
            if (!isAlive()) return;
            step();
        }
    }
}
