package com.redtoast.simulation.LangAPI;

import com.redtoast.Computer;
import com.redtoast.simulation.Runtime;
import org.jetbrains.annotations.NotNull;

public interface APIRegistry {
    public @NotNull API Create(Runtime runtime, Computer computer);
    public default boolean predicate(Runtime runtime, Computer computer){return true;}
}