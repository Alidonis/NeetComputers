package com.redtoast.simulation.LangAPI;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.Parameter.ParameterRules;
import com.redtoast.simulation.LangAPI.ValueTypes.*;
import com.redtoast.simulation.Runtime;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedList;

public class APILoader {
    private Runtime ParentRuntime;
    private static Logger logger = LoggerFactory.getLogger("NeetComputers: API loading");
    private static LinkedList<APIRegistry> APIs = new LinkedList<>();
    public APILoader(Runtime runtime, Computer computer){
        ParentRuntime = runtime;
        load(runtime, computer);
        load(runtime, computer);
    }

    public static void register(APIRegistry registry){
        APIs.add(registry);
    }

    private void load(Runtime runtime, Computer computer){
        for (APIRegistry registry : APIs){
            if (registry.predicate(runtime, computer)){
                API api = registry.Create(runtime, computer);
                Function[] function = translateAPI(api);
                loadIntoGlobals(function, api.getLabel());
            }
        }
    }

    private void loadIntoGlobals(Function[] functions, String label){
        LuaTable apiTable = new LuaTable();
        for (Function func : functions){
            Varargs args = (Varargs) NeetComputers.getTranslater("Lua 5.2").fromValue(func.asValue());
            if (args instanceof LuaValue val){
                apiTable.set(func.getName(), val);
            }
        }
        ParentRuntime.env.set(label, apiTable);
    }

    private static ParameterRules rulesFromMethod(Method method){
        boolean isPacked = method.isVarArgs();
        Parameter[] parameters = method.getParameters();
        ParameterRules rules = new ParameterRules();
        for (int i = 0; i < parameters.length; i++){
            if (i == parameters.length-1 && isPacked){
                VarType type = VarType.ANY;
                if (parameters[i].getType()==String[].class){
                    type = (VarType.STRING);
                }else if (parameters[i].getType()==int[].class){
                    type = (VarType.NUMBER);
                }else if (parameters[i].getType()==double[].class){
                    type = (VarType.NUMBER);
                }else if (parameters[i].getType()==float[].class){
                    type = (VarType.NUMBER);
                }else if (parameters[i].getType()==boolean[].class){
                    type = (VarType.BOOLEAN);
                }else if (parameters[i].getType()==Table[].class){
                    type = (VarType.TABLE);
                }else if (parameters[i].getType()==Tuple[].class){
                    type = (VarType.TUPLE);
                }else if (parameters[i].getType()==List[].class){
                    type = (VarType.LIST);
                }else if (parameters[i].getType()==Function[].class){
                    type = (VarType.FUNCTION);
                }
                if (parameters[i].isAnnotationPresent(CustomRule.class)){
                    try{
                        Constructor<? extends CustomParameter> constructor = parameters[i].getAnnotation(CustomRule.class).rule().getDeclaredConstructor();
                        constructor.setAccessible(true);
                        rules.add(constructor.newInstance(), type);
                    }catch (Throwable e){
                        logger.warn("Failed to load api, threw: "+ e);
                        rules.allowPacking(type);
                    }
                }else{
                    rules.allowPacking(type);
                }
            }else{
                VarType type = VarType.ANY;
                if (parameters[i].getType()==String.class){
                    type = (VarType.STRING);
                }else if (parameters[i].getType()==int.class){
                    type = (VarType.NUMBER);
                }else if (parameters[i].getType()==double.class){
                    type = (VarType.NUMBER);
                }else if (parameters[i].getType()==float.class){
                    type = (VarType.NUMBER);
                }else if (parameters[i].getType()==boolean.class){
                    type = (VarType.BOOLEAN);
                }else if (parameters[i].getType()==Table.class){
                    type = (VarType.TABLE);
                }else if (parameters[i].getType()==Tuple.class){
                    type = (VarType.TUPLE);
                }else if (parameters[i].getType()==List.class){
                    type = (VarType.LIST);
                }else if (parameters[i].getType()==Function.class){
                    type = (VarType.FUNCTION);
                }
                if (parameters[i].isAnnotationPresent(CustomRule.class)){
                    try{
                        Constructor<? extends CustomParameter> constructor = parameters[i].getAnnotation(CustomRule.class).rule().getDeclaredConstructor();
                        constructor.setAccessible(true);
                        rules.add(constructor.newInstance(), type);
                    }catch (Throwable e){
                        logger.warn("Failed to load api, threw: "+e);
                        rules.add(type);
                    }
                }else{
                    rules.add(type);
                }
            }
        }
        return rules;
    }

    private static Object[] processArgs(Method method, FunctionInput input){
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < args.length; i++){
            if (method.getName().equals("getHexColor")) System.out.println(6);
            if (parameters[i].isVarArgs()){
                if (parameters[i].getType()==String.class){
                    List packed = input.getPacked();
                    String[] pack = new String[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof String val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==int.class){
                    List packed = input.getPacked();
                    int[] pack = new int[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof Integer val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==double.class){
                    List packed = input.getPacked();
                    double[] pack = new double[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof Double val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==float.class){
                    List packed = input.getPacked();
                    float[] pack = new float[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof Float val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==boolean.class){
                    List packed = input.getPacked();
                    boolean[] pack = new boolean[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof Boolean val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==Table.class){
                    List packed = input.getPacked();
                    Table[] pack = new Table[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof Table val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==Tuple.class){
                    List packed = input.getPacked();
                    Tuple[] pack = new Tuple[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof Tuple val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==List.class){
                    List packed = input.getPacked();
                    List[] pack = new List[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof List val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==Function.class){
                    List packed = input.getPacked();
                    Function[] pack = new Function[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof Function val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()==Value.class){
                    List packed = input.getPacked();
                    Value[] pack = new Value[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        pack[d] = input.get(i);
                    }
                    args[i] = pack;
                }
            }else{
                if (parameters[i].getType()==String.class){
                    if (input.get(i).getValue() instanceof String val) args[i] = val;
                }else if (parameters[i].getType()==int.class){
                    if (input.get(i).getValue() instanceof Integer val) args[i] = val;
                }else if (parameters[i].getType()==double.class){
                    if (input.get(i).getValue() instanceof Double val) args[i] = val;
                }else if (parameters[i].getType()==float.class){
                    if (input.get(i).getValue() instanceof Float val) args[i] = val;
                }else if (parameters[i].getType()==boolean.class){
                    if (input.get(i).getValue() instanceof Boolean val) args[i] = val;
                }else if (parameters[i].getType()==Table.class){
                    if (input.get(i).getValue() instanceof Table val) args[i] = val;
                }else if (parameters[i].getType()==Tuple.class){
                    if (input.get(i).getValue() instanceof Tuple val) args[i] = val;
                }else if (parameters[i].getType()==List.class){
                    if (input.get(i).getValue() instanceof List val) args[i] = val;
                }else if (parameters[i].getType()==Function.class){
                    if (input.get(i).getValue() instanceof Function val) args[i] = val;
                }else if (parameters[i].getType()==Value.class){
                    args[i] = input.get(i);
                }
            }
        }
        return args;
    }

    private static Function[] translateAPI(Object obj){
        Class<?> _class = obj.getClass();
        LinkedList<Function> functions = new LinkedList<>();
        Method[] buffer = _class.getMethods();
        for (Method method : buffer){
            if (method.isAnnotationPresent(Exposed.class)){
                ParameterRules ruleset = rulesFromMethod(method);
                Function function;
                if (method.getReturnType()==Void.TYPE){
                    function = new Function(ruleset) {
                        @Override
                        public Value call(FunctionInput parameters) {
                            try {
                                method.invoke(obj, processArgs(method, parameters));
                            }catch (Throwable e){
                                Throwable unwrappedThrow = e.getCause();
                                if (unwrappedThrow instanceof LangError){
                                    return Value.asError(unwrappedThrow.getMessage());
                                }else{
                                    Function.logError(unwrappedThrow.getMessage());
                                    return Value.asError("Unexpected java error, check log for information");
                                }
                            }
                            return Value.NULL;
                        }
                    };
                }else if(method.getReturnType()==Value.class){
                    function = new Function(ruleset) {
                        @Override
                        public Value call(FunctionInput parameters) {
                            try {
                                Object retun = method.invoke(obj, processArgs(method, parameters));
                                if (retun==null){
                                    return Value.NULL;
                                }
                                return (Value) retun;
                            }catch (Throwable e){
                                Throwable unwrappedThrow = e.getCause();
                                if (unwrappedThrow instanceof LangError){
                                    return Value.asError(unwrappedThrow.getMessage());
                                }else{
                                    Function.logError(unwrappedThrow.getMessage());
                                    return Value.asError("Unexpected java error, check log for information");
                                }
                            }
                        }
                    };
                }else{
                    function = new Function(ruleset) {
                        @Override
                        public Value call(FunctionInput parameters) {
                            try {
                                Object retun = method.invoke(obj, processArgs(method, parameters));
                                if (method.getName().equals("getHexColor")) System.out.println(7);
                                if (retun==null){
                                    return Value.NULL;
                                }else{
                                    return new Value(retun);
                                }
                            }catch (Throwable e){
                                Throwable unwrappedThrow = e.getCause();
                                if (unwrappedThrow instanceof LangError){
                                    return Value.asError(unwrappedThrow.getMessage());
                                }else{
                                    Function.logError(unwrappedThrow.getMessage());
                                    return Value.asError("Unexpected java error, check log for information");
                                }
                            }
                        }
                    };
                }
                function.setName(method.getName());
                functions.add(function);
            }
        }
        Field[] fields = _class.getFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(InsertAtRuntime.class)){
                System.out.println("yay "+field.getName());
                try{
                    functions.addAll((Collection<? extends Function>) field.get(obj));
                }catch (Throwable ignored){
                    logger.warn("Failed to insert '{}' collection at runtime: {}", field.getName(), ignored.getMessage());
                }
            }else{
                System.out.println("fuck "+field.getName());
            }
        }
        Function[] output = new Function[functions.size()];
        for (int i = 0; i < functions.size(); i++){
            output[i] = functions.get(i);
        }
        return output;
    }
}