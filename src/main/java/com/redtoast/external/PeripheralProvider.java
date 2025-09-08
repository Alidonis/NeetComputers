package com.redtoast.external;

import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.API;

public interface PeripheralProvider {
    boolean kill(Runtime runtime);
    API getAPI(Runtime runtime);
    void tick(Runtime runtime);
}
