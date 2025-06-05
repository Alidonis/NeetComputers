package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.simulation.base.API;
import org.jetbrains.annotations.NotNull;

public interface PeripheralProvider {
    @NotNull
    API attachPeripheral(Computer computer, java.lang.Runtime runtime);
    void detach(Computer computer, java.lang.Runtime runtime);
    @Deprecated default boolean prioritizeOverCC(){return true;}
}
