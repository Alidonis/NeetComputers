package com.redtoast.simulation.base;

import com.redtoast.Computer;
import com.redtoast.computerSpecs;
import com.redtoast.simulation.Runtime;

public interface LanguageGeneric {
    String getVersion();
    LanguageTranslater generateTranslationClass();
    LangThread createThread(String script, Runtime parentRuntime, Computer parentComputer, computerSpecs specifications);
}
