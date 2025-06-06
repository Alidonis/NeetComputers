package com.redtoast.Lua;

import com.redtoast.ComputerSpecs;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.Runtime;
import org.luaj.vm2.LuaError;
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
    private Runtime runtime;
    private LuaGlobals globals;
    protected short ticket = 0;

    public LuaThread(String script, Runtime parentRuntime, ComputerSpecs specification){
        super();
        try{
            globals = new LuaGlobals(parentRuntime.globalManager);
            specs = specification;
            LuaValue chunk = globals.load(script, "LuaThread");
            coroutine = new org.luaj.vm2.LuaThread(globals, chunk);
            globals.LuaDebug.get("sethook").invoke(new LuaValue[]{coroutine,new clockIn(this),LuaValue.NIL,LuaValue.valueOf(specification.BatchSize)});
            runtime = parentRuntime;
        } catch (Exception e) {
            if (e instanceof LuaError laerror){
                kill(laerror.getMessage());
                error(laerror.toString());
            }else{
                kill("Unexpected java issue, please check logs");
                error(e.toString());
            }
        }
    }

    @Override
    public String getLang() {
        return "Lua 5.2";
    }

    @Override
    public void Yield() {
        globals.yield(LuaValue.NIL);
    }

    private void step(){
        Varargs result = coroutine.resume(LuaValue.NIL);
        globals.push();
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
        double util = (double) specs.Batches / runtime.threads.size();
        util *= specs.CoreUtilizationBonus * (runtime.threads.size() - 1) + 1;
        ticket += (short) Math.round(util);
        int threadCount = runtime.threads.size();
        while (ticket>0) {
            if (!isAlive()) return;
            step();
            if (threadCount!=runtime.threads.size() && isAlive()){
                ticket += (short) (Math.round(util) - (specs.CoreUtilizationBonus * (runtime.threads.size() - 1) + 1));
            }
        }
    }
}
