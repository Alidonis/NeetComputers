package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.util.Objects;
import java.util.UUID;

public class IOAPI implements API {
    private final Computer computer;

    public static class WrappedFunction extends Function{
        private final Computer computer;
        private final UUID uuid;

        public WrappedFunction(Computer computer, UUID uuid, String functionName){
            super(computer.getRuntime(), functionName, ParameterRules.ANY);
            this.computer = computer;
            this.uuid = uuid;
        }

        @Override
        public Value call(FunctionInput parameters) {
            if (!computer.isOn() || computer.isCrashed()) return Value.asError("Computer dead, how did you get here?");
            for (PeripheralProvider peripheralProvider : computer.getPeripheralProviders()){
                if (Objects.equals(peripheralProvider.getUuid().toString(), uuid.toString())){
                    return peripheralProvider.callFunction(getName(), parameters.toArray());
                }
            }
            return Value.asError("Peripheral not found");
        }
    }

    @Override
    public String getLabel() {
        return "IO";
    }

    public IOAPI(Computer computer){
        this.computer = computer;
    }

    @Exposed
    public List getPeripherals(){
        List list = new List();
        for (PeripheralProvider peripheralProvider : computer.getPeripheralProviders()){
            list.add(Value.of(peripheralProvider.getUuid().toString()));
        }
        return list;
    }

    @Exposed
    public String getPeripheralType(String uuidString){
        try {
            UUID.fromString(uuidString);
        }catch (IllegalArgumentException illegalArgumentException){
            throw new ExposedError("UUID invalidly formatted");
        }
        for (PeripheralProvider peripheralProvider : computer.getPeripheralProviders()){
            if (Objects.equals(peripheralProvider.getUuid().toString(), uuidString)) return peripheralProvider.getTypeName();
        }
        throw new ExposedError("Peripheral not found");
    }

    @Exposed
    public Table wrapPeripheral(String uuidString){
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        }catch (IllegalArgumentException illegalArgumentException){
            throw new ExposedError("UUID invalidly formatted");
        }
        for (PeripheralProvider peripheralProvider : computer.getPeripheralProviders()){
            if (Objects.equals(peripheralProvider.getUuid().toString(), uuidString)){
                Table table = new Table();
                for (String functionName : peripheralProvider.getFunctionNames()){
                    table.put(functionName, new WrappedFunction(computer, uuid, functionName).asValue());
                }
                return table;
            }
        }
        throw new ExposedError("Peripheral not found");
    }

    @Exposed
    public Value callFunction(String uuidString, String functionName, Value... args){
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        }catch (IllegalArgumentException illegalArgumentException){
            throw new ExposedError("UUID invalidly formatted");
        }
        for (PeripheralProvider peripheralProvider : computer.getPeripheralProviders()){
            if (Objects.equals(peripheralProvider.getUuid().toString(), uuidString)){
                for (String functionName2 : peripheralProvider.getFunctionNames()){
                    if (functionName.equals(functionName2)) return peripheralProvider.callFunction(functionName, args);
                }
            }
        }
        throw new ExposedError("Peripheral not found");
    }
}
