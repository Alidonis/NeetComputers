package com.redtoast.simulation.base;

import com.redtoast.Computer;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.config.ComputerConfig;

public interface LanguageGeneric {
    String getName();
    LangThread createThread(String script, Runtime parentRuntime, Computer parentComputer, ComputerConfig specifications);
    boolean bumpIndexs();
}
