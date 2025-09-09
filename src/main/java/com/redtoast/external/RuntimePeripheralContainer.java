package com.redtoast.external;

import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class RuntimePeripheralContainer {
    public AtomicBoolean discovered = new AtomicBoolean(true);
    public final TokenizedPeripheral peripheral;
    public final Table table;
    public final UUID uuid;

    public RuntimePeripheralContainer(TokenizedPeripheral peripheral, Runtime runtime){
        this.peripheral = peripheral;
        table = APILoader.TableizeAPIWithCloseCondition(peripheral.getAPI(), discovered, runtime);
        table.put("_uuid", this.peripheral.getProviderUuid().toString());
        uuid = UUID.randomUUID();
    }

    public RuntimePeripheralContainer(PeripheralProvider peripheral, Runtime runtime){
        this.peripheral = new TokenizedPeripheral(runtime.parent, peripheral);
        table = APILoader.TableizeAPIWithCloseCondition(this.peripheral.getAPI(), discovered, runtime);
        table.put("_uuid", this.peripheral.getProviderUuid().toString());
        uuid = UUID.randomUUID();
    }

    public UUID getProviderUuid(){
        return peripheral.getProviderUuid();
    }
}
