package com.redtoast.Lua;

import com.redtoast.Computer;
import com.redtoast.ComputerSpecs;
import com.redtoast.simulation.LangAPI.LanguageTranslater;
import com.redtoast.simulation.LangThread;
import com.redtoast.simulation.LanguageGeneric;
import com.redtoast.simulation.Runtime;

public class LuaMaster implements LanguageGeneric {
    @Override
    public String getVersion() {
        return "Lua 5.2";
    }

    @Override
    public LanguageTranslater generateTranslationClass() {
        return new LuaTranslater();
    }

    @Override
    public LangThread createThread(String script, Runtime parentRuntime, Computer parentComputer, ComputerSpecs specifications) {
        return new LuaThread(script, parentRuntime, parentComputer, specifications);
    }
}
