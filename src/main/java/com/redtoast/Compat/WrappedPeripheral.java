package com.redtoast.Compat;

import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Function;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public class WrappedPeripheral implements PeripheralProvider {
    private final IPeripheral peripheral;
    private final String[] functionNames;
    private final Hashtable<Method, Function> functionLookup = new Hashtable<>();
    private final BlockPos pos;

    public WrappedPeripheral(IPeripheral peripheral, BlockPos pos, Computer computer){
        this.peripheral = peripheral;
        Runtime runtime = computer.getRuntime();
        Class<?> clazz = peripheral.getClass();
        Method[] functions = clazz.getMethods();
        LinkedList<String> names = new LinkedList<>();
        for (Method method : functions){
            if (method.isAnnotationPresent(LuaFunction.class)){
                LuaFunction annotation = method.getAnnotation(LuaFunction.class);
                if (annotation.value().length==0){
                    names.add(method.getName());
                }else{
                    names.addAll(Arrays.asList(annotation.value()));
                }
                Function buffer = APILoader.sandboxFunction(method, peripheral, ParameterRules.ANY, runtime);
                //if (annotation.mainThread()) buffer.makeMainThread();
                functionLookup.put(method, buffer);
            }
        }
        functionNames = names.toArray(new String[0]);
        this.pos = pos;

        peripheral.attach(new ComputerWrapper(computer, peripheral));
    }

    @Override
    public String[] getFunctionNames() {
        return functionNames;
    }

    @Override
    public Value<?> callFunction(Runtime runtime, String name, Value<?>... Args) {
        Class<?> clazz = peripheral.getClass();
        Method[] functions = clazz.getMethods();
        for (Method method : functions){
            if (method.isAnnotationPresent(LuaFunction.class)){
                LuaFunction annotation = method.getAnnotation(LuaFunction.class);
                LinkedList<String> names = new LinkedList<>();
                if (annotation.value().length==0){
                    names.add(method.getName());
                }else{
                    names.addAll(Arrays.asList(annotation.value()));
                }
                for (String string : names){
                    if (string.equals(name)) {
                        return functionLookup.get(method).invoke(FunctionInput.fromArray(Args));
                    }
                }
            }
        }
        return Value.asError("Cant Find Function '"+name+"'");
    }

    @Override
    public String getTypeName() {
        return peripheral.getType();
    }

    @Override
    public UUID getUuid() {
        return UUID.nameUUIDFromBytes(Long.toOctalString(pos.asLong()).getBytes());
    }

    @Override
    public @Nullable String getTag() {
        return null;
    }

    @Override
    public void setTag(@NotNull String tag) {

    }

    @Override
    public void computerAttached(Computer computer) {

    }

    @Override
    public void computerDetached(Computer computer) {

    }
}
