package com.redtoast.APIS;

import com.redtoast.external.PeripheralProvider;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.util.Hashtable;
import java.util.UUID;

public class ExternalAPI implements API {
    private Hashtable<UUID, Table> cache = new Hashtable<>();

    @Override
    public String getLabel() {
        return "external";
    }

    public void onAttached(PeripheralProvider peripheral){

    }

    public void onDettached(PeripheralProvider peripheralProvider){

    }

    private Table getPeripheralRaw(UUID uuid, PeripheralProvider peripheralProvider){
        return null;
    }
}
