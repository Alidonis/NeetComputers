package com.redtoast.simulation.base;

import com.redtoast.Computer;
import com.redtoast.ComputerSpecs;
import com.redtoast.simulation.Runtime;

public interface LanguageGeneric {
    String getVersion();
    LanguageTranslater generateTranslationClass();
    LangThread createThread(String script, Runtime parentRuntime, Computer parentComputer, ComputerSpecs specifications);
}
