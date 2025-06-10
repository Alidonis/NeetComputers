package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.simulation.base.API;
import org.jetbrains.annotations.NotNull;

public interface APIRegistry {
    public @NotNull API Create(Computer computer);
    public default boolean predicate(Computer computer){return true;}
}