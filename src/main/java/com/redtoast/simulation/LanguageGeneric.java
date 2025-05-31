package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.ComputerSpecs;
import com.redtoast.simulation.LangAPI.LanguageTranslater;

public interface LanguageGeneric {
    String getVersion();
    LanguageTranslater generateTranslationClass();
    LangThread createThread(String script, Runtime parentRuntime, Computer parentComputer, ComputerSpecs specifications);
}
