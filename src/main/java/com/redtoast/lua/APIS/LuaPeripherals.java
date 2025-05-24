package com.redtoast.lua.APIS;

import com.redtoast.lua.LuaAPI;
import com.redtoast.lua.peripheral.peripheralWrapper;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.LinkedList;
import java.util.Objects;

public abstract class LuaPeripherals extends LuaAPI {
    public LuaPeripherals() {
        super("peripherals");

        LuaTable metaUUID = new LuaTable();
        metaUUID.set("hasUUID", LuaValue.TRUE);
        metaUUID.set("UUID_source", "peripheral");
        metaUUID.set("tag","uuid");

        Rules.Rule validPeripheral = new Rules.Rule() {
            @Override
            public boolean rule(LuaValue arg) {
                if (!arg.istable()) return false;
                LuaTable table = arg.checktable();
                if (table.getmetatable().isnil()) return true;
                if (table.getmetatable().get("tag").isnil()) return false;
                return table.getmetatable().get("tag").toString().equals("peripheral");
            }
        };

        set("isPresent", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                String uuid = args[0].toString();
                LinkedList<peripheralWrapper> periphs = getParentsPeripherals();
                for (int i = 0; i < periphs.size(); i++){
                    if (Objects.equals(periphs.get(i).uuid.toString(), uuid)){
                        return LuaValue.TRUE;
                    }
                }
                return LuaValue.FALSE;
            }

            @Override
            public Rules getRules() {
                return new Rules("string");
            }
        });
        set("isPeripheral", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                LuaTable table = args[0].checktable();
                if (table.getmetatable()==null) return LuaValue.FALSE;
                if (table.getmetatable().get("tag").isnil()) return LuaValue.FALSE;
                if (table.getmetatable().get("tag").toString().equals("peripheral")){
                    return LuaValue.TRUE;
                }else{
                    return LuaValue.FALSE;
                }
            }

            @Override
            public Rules getRules() {
                return new Rules("table");
            }
        });
        set("getUUID", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                LuaTable peripheral = args[0].checktable();
                LuaValue uuid = LuaValue.valueOf(peripheral.getmetatable().get("uuid").toString());
                uuid.setmetatable(metaUUID);
                return uuid;
            }

            @Override
            public Rules getRules() {
                return new Rules(validPeripheral, "peripheral");
            }
        });
        set("getType", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                LuaTable peripheral = args[0].checktable();
                return peripheral.getmetatable().get("type");
            }

            @Override
            public Rules getRules() {
                return new Rules(validPeripheral, "peripheral");
            }
        });
        set("retrieve", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                String uuid = args[0].toString();
                LinkedList<peripheralWrapper> periphs = getParentsPeripherals();
                for (int i = 0; i < periphs.size(); i++){
                    if (Objects.equals(periphs.get(i).uuid.toString(), uuid)){
                        return periphs.get(i).table;
                    }
                }
                return LuaValue.NIL;
            }

            @Override
            public Rules getRules() {
                return new Rules("string");
            }
        });
        set("find", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                String type = args[0].toString();
                LinkedList<peripheralWrapper> periphs = getParentsPeripherals();
                LinkedList<peripheralWrapper> output = new LinkedList<>();
                for (peripheralWrapper periph : periphs) {
                    if (periph.peripheralType.equals(type)) {
                        if (true){//(args[1].isnil()) {
                            output.add(periph);
                        } else {
                            Varargs outputs = args[1].checkfunction().invoke(new LuaValue[]{LuaValue.valueOf(periph.uuid.toString()), periph.table});
                            if (outputs.narg() > 0) {
                                if (outputs.arg1().isboolean()) {
                                    if (outputs.arg1().toboolean()) {
                                        output.add(periph);
                                    }
                                } else {
                                    return LuaValue.error("filter function expected boolean, got " + outputs.arg1().typename());
                                }
                            } else {
                                return LuaValue.error("filter function expected output, got none");
                            }
                        }
                    }
                }
                LuaValue[] outputArray = new LuaValue[output.size()];
                for (int i = 0; i < output.size(); i++){
                    outputArray[i] = output.get(i).table;
                }
                return LuaValue.listOf(outputArray);
            }

            @Override
            public Rules getRules() {
                return new Rules("string");//.add("function",true);
            }
        });
        set("getAll", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                LuaValue[] uuids = new LuaValue[getParentsPeripherals().size()];
                for (int i = 0; i < getParentsPeripherals().size(); i++){
                    uuids[i] = LuaValue.valueOf(getParentsPeripherals().get(i).uuid.toString());
                    uuids[i].setmetatable(metaUUID);
                }
                return LuaValue.listOf(uuids);
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });
    }
    public abstract LinkedList<peripheralWrapper> getParentsPeripherals();
}
