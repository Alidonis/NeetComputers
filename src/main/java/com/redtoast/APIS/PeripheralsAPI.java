package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import com.redtoast.simulation.value.ValueTypes.Tuple;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.Peripheral;

import java.util.LinkedList;
import java.util.Objects;

public class PeripheralsAPI implements API {
    Runtime runtime;
    Table metaUUID = new Table();

    @Override
    public String getLabel() {
        return "peripherals";
    }

    public PeripheralsAPI(Computer vm) {
        runtime = vm.getRuntime();
        metaUUID.put("hasUUID", Value.TRUE);
        metaUUID.put("UUID_source", "peripheral");
        metaUUID.put("tag","uuid");
    }

    @Exposed
    public boolean exists(String uuid){
        LinkedList<Peripheral> periphs = runtime.getPeripherals();
        for (int i = 0; i < periphs.size(); i++){
            if (Objects.equals(periphs.get(i).uuid.toString(), uuid)){
                return true;
            }
        }
        return false;
    }

    @Exposed
    public boolean isPeripheral(Table table){
        if (table.asValue().hasMetaTable()) return false;
        if (table.asValue().getMetaTable("tag").isNull()) return false;
        if (table.asValue().getMetaTable("tag").toString().equals("peripheral")){
            return true;
        }else{
            return false;
        }
    }

    @Exposed
    public String getUUID(Table peripheral){
        Value uuid = Value.of(peripheral.asValue().getMetaTable("uuid").toString());
        uuid.setMetaTable(metaUUID);
        return uuid.toString();
    }

    @Exposed
    public String getType(Table peripheral){
        return peripheral.asValue().getMetaTable("type").toString();
    }

    @Exposed
    public Table retrieve(String uuid){
        LinkedList<Peripheral> periphs = runtime.getPeripherals();
        for (int i = 0; i < periphs.size(); i++){
            if (Objects.equals(periphs.get(i).uuid.toString(), uuid)){
                return periphs.get(i).table.getValue();
            }
        }
        return null;
    }

    @Exposed
    public Tuple locate(String type){
        LinkedList<Peripheral> periphs = runtime.getPeripherals();
        LinkedList<Peripheral> output = new LinkedList<>();
        for (Peripheral periph : periphs) {
            if (periph.peripheralType.equals(type)) {
                output.add(periph);
            }
        }
        Value[] outputArray = new Value[output.size()];
        for (int i = 0; i < output.size(); i++){
            outputArray[i] = output.get(i).table;
        }
        return new Tuple(outputArray);
    }

    @Exposed
    public List getAll(){
        Value[] uuids = new Value[runtime.getPeripherals().size()];
        for (int i = 0; i < runtime.getPeripherals().size(); i++){
            uuids[i] = Value.of(runtime.getPeripherals().get(i).uuid.toString());
            uuids[i].setMetaTable(metaUUID);
        }
        return new List(uuids);
    }
}
