package com.redtoast.Lua;

import com.redtoast.computerSpecs;
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
            parent.yield();
            return LuaValue.NIL;
        }
    }

    private computerSpecs specs;
    private org.luaj.vm2.LuaThread coroutine;
    private LuaValue chunk;
    private Runtime runtime;
    private LuaGlobals globals;
    protected short ticket = 0;

    public LuaThread(String script, Runtime parentRuntime, computerSpecs specification){
        super();
        specs = specification;
        runtime = parentRuntime;
        try{
            globals = new LuaGlobals(runtime.getGlobals());
            chunk = globals.load(script, "LuaThread");
            coroutine = new org.luaj.vm2.LuaThread(globals, chunk);
            globals.LuaDebug.get("sethook").invoke(new LuaValue[]{coroutine,new clockIn(this),LuaValue.NIL,LuaValue.valueOf(specs.BatchSize)});
        } catch (Exception e) {
            if (e instanceof LuaError error){
                kill(error.getMessage());
                error(error.toString());
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
    public void yield() {
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
                error("Lua 5.2 threw " + result.arg(2).toString());
            }
            kill();
        }
    }

    @Override
    public void tick(){
        clearJavaLag();
        if (!isAlive()) return;
        ticket += specs.Batches;
        while (ticket>0) {
            if (runtime.getParent().isCrashed()) kill("Parent computer crashed");
            if (runtime.shouldDie()) {
                kill("host stopped");
                runtime.getParent().stop();
            }
            if (!isAlive()) return;
            step();
            if (isAlive()){
                short tax = (short) getJavaTaxBulk((short) (10*specs.BatchSize));
                ticket -= (short) (tax * 25);
            }
        }
    }

    @Override
    public String getSource(){
        return "lua:"+globals.debuglib.traceback(1).split(":")[2];
    }

    @Override
    public void crash(String message) {
        runtime.getParent().crash(message);
    }
}
