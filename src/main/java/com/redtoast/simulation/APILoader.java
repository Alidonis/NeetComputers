package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.simulation.annotations.*;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterCheckReturn;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.base.LangError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Exception;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.VarType;
import com.redtoast.simulation.value.ValueTypes.*;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.CustomParameter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class APILoader {
    private final Runtime ParentRuntime;
    private static final Logger logger = LoggerFactory.getLogger("NeetComputers: API loading");
    private static final LinkedList<APIRegistry> APIs = new LinkedList<>();

    /**
     * APILoader instances are created by individual Runtime's
     */
    public APILoader(Computer computer){
        ParentRuntime = computer.getRuntime();
        load(computer.getRuntime(), computer);
    }

    //added a APIRegistery object to the static list of API's to instanciate
    public static void register(APIRegistry registry){
        APIs.add(registry);
    }

    private void load(Runtime runtime, Computer computer){
        for (APIRegistry registry : APIs){
            if (registry.predicate(computer)){
                API api = registry.Create(computer);
                Function[] function = translateAPI(api, runtime);
                loadIntoGlobals(function, api.getLabel());
            }
        }
    }

    private void loadIntoGlobals(Function[] functions, String label){
        Table apiTable = new Table();
        for (Function func : functions){
            if (func.getName()!=null){
                apiTable.put(func.getName(), func.asValue());
            }else{
                logger.warn("Issue encountered loading api '{}': nameless function (try .setName on runtime implimented functions)", label);
            }
        }
        ParentRuntime.globalManager.put(label, apiTable.asValue());
    }

    public static Table TableizeAPI(API api, @Nullable Runtime runtime){
        Function[] functions = translateAPI(api, runtime);
        Table apiTable = new Table();
        for (Function func : functions){
            if (func.getName()!=null){
                apiTable.put(func.getName(), func.asValue());
            }else{
                logger.warn("Issue encountered loading api '{}': nameless function (try .setName on runtime implimented functions)", api.getLabel());
            }
        }
        return apiTable;
    }

    public static Peripheral WrapAPI(API api, @Nullable Runtime runtime){
        Function[] functions = translateAPI(api, runtime);
        Table apiTable = new Table();
        for (Function func : functions){
            if (func.getName()!=null){
                apiTable.put(func.getName(), func.asValue());
            }else{
                logger.warn("Issue encountered loading api '{}': nameless function (try .setName on runtime implimented functions)", api.getLabel());
            }
        }
        return new Peripheral(api.getLabel(), apiTable);
    }

    //background methods used to process API's
    private static ParameterRules rulesFromMethod(Method method, Hashtable<String, CustomParameter> customParameters){
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
                    String rulename = parameters[i].getAnnotation(CustomRule.class).rule();
                    if (customParameters.contains(rulename)){
                        rules.allowPacking(customParameters.get(rulename), type);
                    }else{
                        logger.warn("Failed to enforce custom rule '{}', class not found", rulename);
                        rules.allowPacking(type);
                    }
                }else{
                    rules.allowPacking(type);
                    if (parameters[i].isAnnotationPresent(Index.class)){
                        rules.packRule.giveIndexOffset(parameters[i].getAnnotation(Index.class).offset(), parameters[i].getAnnotation(Index.class).strict());
                    }
                    if (parameters[i].isAnnotationPresent(Range.class)){
                        rules.packRule.setRange(parameters[i].getAnnotation(Range.class).range());
                    }
                }
                rules.packRule.disName = parameters[i].getName();
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
                    String rulename = parameters[i].getAnnotation(CustomRule.class).rule();
                    if (customParameters.get(rulename)!=null){
                        rules.add(customParameters.get(rulename), type);
                    }else{
                        logger.warn("Failed to enforce custom rule '{}', class not found", rulename);
                        rules.add(type);
                    }
                }else{
                    rules.add(type);
                    if (parameters[i].isAnnotationPresent(Index.class)){
                        rules.rules.getLast().giveIndexOffset(parameters[i].getAnnotation(Index.class).offset(), parameters[i].getAnnotation(Index.class).strict());
                    }
                    if (parameters[i].isAnnotationPresent(Range.class)){
                        rules.rules.getLast().setRange(parameters[i].getAnnotation(Range.class).range());
                    }
                }
                rules.rules.getLast().disName = parameters[i].getName();
            }
        }
        return rules;
    }

    private static Object[] processArgs(Method method, FunctionInput input, @Nullable Runtime runtime){
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < args.length; i++){
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
                        int offset = parameters[i].isAnnotationPresent(Index.class) ? (runtime!=null ? (runtime.thread.getLang().equals("Lua 5.2") ? 1 : 0) : 0) + parameters[i].getAnnotation(Index.class).offset() : 0;//the road to hell is paved with good intentions
                        if (input.get(i).getValue() instanceof Integer val) pack[d] = val - offset;
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
                }else if (parameters[i].getType()== Value.class){
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
                    int offset = parameters[i].isAnnotationPresent(Index.class) ? (runtime!=null ? (runtime.thread.getLang().equals("Lua 5.2") ? 1 : 0) : 0) + parameters[i].getAnnotation(Index.class).offset() : 0;//the road to hell is paved with good intentions, twice
                    args[i] = input.get(i).toInt() - offset;
                }else if (parameters[i].getType()==double.class){
                    args[i] = input.get(i).toDouble();
                }else if (parameters[i].getType()==float.class){
                    args[i] = input.get(i).toFloat();
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

    private static Function[] translateAPI(API obj, @Nullable Runtime runtime){
        Class<?> _class = obj.getClass();
        Hashtable<String, LinkedList<Function>> functions = new Hashtable<>();
        Hashtable<String, CustomParameter> customParameters = new Hashtable<>();
        Class<?>[] classes = _class.getDeclaredClasses();
        for (Class<?> clazz : classes){
            if (clazz.isAnnotationPresent(CustomRule.class)){
                if (clazz.getSuperclass()==CustomParameter.class){
                    try{
                        Constructor<? extends CustomParameter> constructor = (Constructor<? extends CustomParameter>) clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        customParameters.put(clazz.getAnnotation(CustomRule.class).rule(),constructor.newInstance());
                    }catch (Throwable e){
                        logger.warn("Failed to load custom rule, threw: "+e);
                    }
                }else{
                    logger.warn("Failed to establish custom rule '{}', doesn't extend CustomParameter", clazz.getAnnotation(CustomRule.class).rule());
                }
            }
        }
        Method[] buffer = _class.getMethods();
        for (Method method : buffer){
            if (method.isAnnotationPresent(Exposed.class)){
                ParameterRules ruleset = rulesFromMethod(method, customParameters);
                Function function;
                if (method.getReturnType()==Void.TYPE){
                    function = new Function(ruleset) {
                        @Override
                        public Value call(FunctionInput parameters) {
                            if (runtime!=null){
                                obj.onCall(runtime.thread);
                            }
                            try {
                                method.invoke(obj, processArgs(method, parameters, runtime));
                            }catch (Throwable e){
                                Throwable unwrappedThrow = e.getCause();
                                if (unwrappedThrow instanceof LangError){
                                    return Value.asError(unwrappedThrow.getMessage());
                                }else{
                                    for (StackTraceElement track : unwrappedThrow.getStackTrace()){
                                        System.out.println("NC ["+track.getLineNumber()+"]: "+track);
                                    }
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
                            if (runtime!=null){
                                obj.onCall(runtime.thread);
                            }
                            try {
                                Object retun = method.invoke(obj, processArgs(method, parameters, runtime));
                                if (retun==null){
                                    return Value.NULL;
                                }
                                return (Value) retun;
                            }catch (Throwable e){
                                Throwable unwrappedThrow = e.getCause();
                                if (unwrappedThrow instanceof LangError){
                                    return Value.asError(unwrappedThrow.getMessage());
                                }else{
                                    for (StackTraceElement track : unwrappedThrow.getStackTrace()){
                                        System.out.println("NC ["+track.getLineNumber()+"]: "+track);
                                    }
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
                            if (runtime!=null){
                                obj.onCall(runtime.thread);
                            }
                            try {
                                Object retun = method.invoke(obj, processArgs(method, parameters, runtime));
                                if (retun==null){
                                    return Value.NULL;
                                }else{
                                    return Value.of(retun);
                                }
                            }catch (Throwable e){
                                Throwable unwrappedThrow = e.getCause();
                                if (unwrappedThrow instanceof LangError){
                                    return Value.asError(unwrappedThrow.getMessage());
                                }else{
                                    for (StackTraceElement track : unwrappedThrow.getStackTrace()){
                                        System.out.println("NC ["+track.getLineNumber()+"]: "+track);
                                    }
                                    Function.logError(unwrappedThrow.getMessage());
                                    return Value.asError("Unexpected java error, check log for information");
                                }
                            }
                        }
                    };
                }
                if (method.getAnnotation(Exposed.class).nameOverride().isBlank()){
                    function.setName(method.getName());
                }else{
                    function.setName(method.getAnnotation(Exposed.class).nameOverride());
                }
                try{
                    functions.get(function.getName()).add(function);
                }catch (java.lang.Exception weenier){
                    LinkedList<Function> funcy = new LinkedList<>();
                    funcy.add(function);
                    functions.put(function.getName(), funcy);
                }
            }
        }
        Field[] fields = _class.getFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(InsertAtRuntime.class)){
                try{
                    for (Function function : (Collection<? extends Function>) field.get(obj)){
                        if (functions.contains(function.getName())){
                            functions.get(function.getName()).add(function);
                        }else{
                            LinkedList<Function> funcy = new LinkedList<>();
                            funcy.add(function);
                            functions.put(function.getName(), funcy);
                        }
                    };
                }catch (Throwable notignored){
                    logger.warn("Failed to insert '{}' collection at runtime: {}", field.getName(), notignored);
                }
            }
        }
        Function[] output = new Function[functions.size()];
        AtomicInteger i = new AtomicInteger();
        functions.forEach((key, values) -> {
            if (values.size()==1){
                output[i.get()] = values.getFirst();
            }else{
                output[i.get()] = new Function(key, ParameterRules.ANY) {
                    @Override
                    public Value call(FunctionInput parameters) {
                        LinkedList<String> errors = new LinkedList<>();
                        LinkedList<String> names = new LinkedList<>();
                        for (Function function : values){
                            ParameterCheckReturn retur = ParameterRules.checkParameters(parameters.toArray(), function.getRules(), runtime);
                            if (!retur.isError()){
                                return function.call(retur.getFunctionInput());
                            }else{
                                errors.add(retur.getMessage());
                                names.add(key + function.getRules().toString(runtime));
                            }
                        }
                        names.sort(String::compareTo);
                        StringBuilder error = new StringBuilder(errors.get(new Random().nextInt(errors.size())));
                        for (String string : names){
                            error.append('\n');
                            error.append(string);
                        }
                        return new Exception(error.toString()).asValue();
                    }
                };
            }
            i.getAndIncrement();
        });
        return output;
    }
}