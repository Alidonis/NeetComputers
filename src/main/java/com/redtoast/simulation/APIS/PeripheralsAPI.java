package com.redtoast.simulation.APIS;

import com.redtoast.simulation.LangAPI.LangAPI;
import com.redtoast.simulation.LangAPI.LambdaFunction;
import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.Parameter.LambdaRule;
import com.redtoast.simulation.LangAPI.Parameter.ParameterRules;
import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.ValueTypes.Table;
import com.redtoast.simulation.LangAPI.VarType;
import com.redtoast.simulation.peripheral.peripheralWrapper;

import java.util.LinkedList;
import java.util.Objects;

public abstract class PeripheralsAPI extends LangAPI {
    public PeripheralsAPI() {
        super("peripherals");

        Table metaUUID = new Table();
        metaUUID.put("hasUUID", Value.TRUE);
        metaUUID.put("UUID_source", "peripheral");
        metaUUID.put("tag","uuid");

        LambdaRule validPeripheral = new LambdaRule() {
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
                return "Valid Peripheral (Table)";
            }
        };

        set("isPresent", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String uuid = args.get(0).toString();
                LinkedList<peripheralWrapper> periphs = getParentsPeripherals();
                for (int i = 0; i < periphs.size(); i++){
                    if (Objects.equals(periphs.get(i).uuid.toString(), uuid)){
                        return Value.TRUE;
                    }
                }
                return Value.FALSE;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.STRING);
            }
        });
        set("isPeripheral", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                Table table = args.get(0).toTable();
                if (table.asValue().hasMetadata()) return Value.FALSE;
                if (table.asValue().getMeta("tag").isNull()) return Value.FALSE;
                if (table.asValue().getMeta("tag").toString().equals("peripheral")){
                    return Value.TRUE;
                }else{
                    return Value.FALSE;
                }
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.TABLE);
            }
        });
        set("getUUID", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                Table peripheral = args.get(0).toTable();
                Value uuid = new Value(peripheral.asValue().getMeta("uuid").toString());
                uuid.setMetaTable(metaUUID);
                return uuid;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(validPeripheral);
            }
        });
        set("getType", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                Table peripheral = args.get(0).toTable();
                return peripheral.asValue().getMeta("type");
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(validPeripheral);
            }
        });
        set("retrieve", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String uuid = args.get(0).toString();
                LinkedList<peripheralWrapper> periphs = getParentsPeripherals();
                for (int i = 0; i < periphs.size(); i++){
                    if (Objects.equals(periphs.get(i).uuid.toString(), uuid)){
                        return periphs.get(i).table;
                    }
                }
                return Value.NULL;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.STRING);
            }
        });
        set("search", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String type = args.get(0).toString();
                LinkedList<peripheralWrapper> periphs = getParentsPeripherals();
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
                return Value.toTuple(outputArray);
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.STRING);
            }
        });
        set("getAll", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                Value[] uuids = new Value[getParentsPeripherals().size()];
                for (int i = 0; i < getParentsPeripherals().size(); i++){
                    uuids[i] = new Value(getParentsPeripherals().get(i).uuid.toString());
                    uuids[i].setMetaTable(metaUUID);
                }
                return new Value(uuids);
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });
    }
    public abstract LinkedList<peripheralWrapper> getParentsPeripherals();
}
