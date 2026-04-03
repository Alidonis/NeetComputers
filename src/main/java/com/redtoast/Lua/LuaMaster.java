package com.redtoast.Lua;

import com.redtoast.Computer;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.base.LanguageGeneric;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.config.ComputerConfig;

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
    public LangThread createThread(String script, Runtime parentRuntime, Computer parentComputer, ComputerConfig specifications) {
        return new LuaThread(script, parentRuntime, specifications);
    }

    @Override
    public boolean bumpIndexs() {
        return true;
    }

    @Override
    public boolean dynamicNumbers() {
        return true;
    }
}
