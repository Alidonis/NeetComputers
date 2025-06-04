package com.redtoast.simulation.APIS;

import com.redtoast.simulation.LangAPI.*;
import com.redtoast.simulation.LangAPI.ValueTypes.List;
import com.redtoast.simulation.LangAPI.ValueTypes.Table;
import com.redtoast.simulation.LangAPI.ValueTypes.Tuple;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.peripheral.peripheralWrapper;

import java.util.LinkedList;
import java.util.Objects;

public class PeripheralsAPI implements API {
    Runtime runtime;
    Table metaUUID = new Table();

    @Override
    public String getLabel() {
        return "peripherals";
    }

    static class validPeripheral extends CustomParameter{
        @Override
        public boolean rule(Value arg) {
            if (!arg.instanceOf(VarType.TABLE)) return false;
            Table table = (Table) arg.getValue();
            if (table.asValue().hasMetadata()) return true;
            if (table.asValue().getMeta("tag").isNull()) return false;
            return table.asValue().getMeta("tag").toString().equals("peripheral");
        }

        @Override
        public String getName() {
            return "Valid Peripheral";
        }
    }

    public PeripheralsAPI(Runtime vm) {
        runtime = vm;
        metaUUID.put("hasUUID", Value.TRUE);
        metaUUID.put("UUID_source", "peripheral");
        metaUUID.put("tag","uuid");
    }

    @Exposed
    public boolean exists(String uuid){
        LinkedList<peripheralWrapper> periphs = runtime.getParentsPeripherals();
        for (int i = 0; i < periphs.size(); i++){
            if (Objects.equals(periphs.get(i).uuid.toString(), uuid)){
                return true;
            }
        }
        return false;
    }

    @Exposed
    public boolean isPeripheral(Table table){
        if (table.asValue().hasMetadata()) return false;
        if (table.asValue().getMeta("tag").isNull()) return false;
        if (table.asValue().getMeta("tag").toString().equals("peripheral")){
            return true;
        }else{
            return false;
        }
    }

    @Exposed
    public String getUUID(@CustomRule(rule = validPeripheral.class) Table peripheral){
        Value uuid = new Value(peripheral.asValue().getMeta("uuid").toString());
        uuid.setMetaTable(metaUUID);
        return uuid.toString();
    }

    @Exposed
    public String getType(@CustomRule(rule = validPeripheral.class) Table peripheral){
        return peripheral.asValue().getMeta("type").toString();
    }

    @Exposed
    public Table retrieve(String uuid){
        LinkedList<peripheralWrapper> periphs = runtime.getParentsPeripherals();
        for (int i = 0; i < periphs.size(); i++){
            if (Objects.equals(periphs.get(i).uuid.toString(), uuid)){
                return periphs.get(i).table.getValue();
            }
        }
        return null;
    }

    @Exposed
    public Tuple search(String type){
        LinkedList<peripheralWrapper> periphs = runtime.getParentsPeripherals();
        LinkedList<peripheralWrapper> output = new LinkedList<>();
        for (peripheralWrapper periph : periphs) {
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
        Value[] uuids = new Value[runtime.getParentsPeripherals().size()];
        for (int i = 0; i < runtime.getParentsPeripherals().size(); i++){
            uuids[i] = new Value(runtime.getParentsPeripherals().get(i).uuid.toString());
            uuids[i].setMetaTable(metaUUID);
        }
        return new List(uuids);
    }
}
