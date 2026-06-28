package com.redtoast.simulation;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.simulation.annotations.*;
import com.redtoast.simulation.base.*;
import com.redtoast.simulation.cache.LoaderCache;
import com.redtoast.simulation.cache.PackedFunctionCache;
import com.redtoast.simulation.cache.StaticFunctionCache;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterCheckReturn;
import com.redtoast.simulation.parameter.ParameterRule;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Exception;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.VarType;
import com.redtoast.simulation.value.ValueTypes.*;
import dan200.computercraft.api.lua.Coerced;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class APILoader {
    private final Runtime ParentRuntime;
    public static final Logger profiler = LoggerFactory.getLogger("NeetComputers: Profiler");
    public static final Logger errorLogger = LoggerFactory.getLogger("NeetComputers: Runtime Java Errors");
    private static final Hashtable<Class<? extends Exposable>, LoaderCache> cache = new Hashtable<>();
    private static final LinkedList<APIRegistry> APIs = new LinkedList<>();

    public static class LoaderError extends Throwable {
        public final String message;
        public LoaderError(String message){
            this.message = message;
        }
    }

    /**
     * APILoader instances are created by individual Runtime's
     */
    public APILoader(Computer computer){
        ParentRuntime = computer.getRuntime();
        load(computer.getRuntime(), computer);
    }

    public static boolean preemptiveCache(Class<? extends Exposable> clazz){
        if (cache.containsKey(clazz)) return false;
        try{
            Hashtable<String, StaticFunctionCache> functions = new Hashtable<>();
            Hashtable<String, PackedFunctionCache> packedFunctions = new Hashtable<>();
            Method[] buffer = clazz.getMethods();
            for (Method method : buffer){
                if (method.isAnnotationPresent(Exposed.class)){
                    ParameterRules ruleset = rulesFromMethod(method);
                    String funcname = method.getAnnotation(Exposed.class).nameOverride().isBlank() ? method.getName() : method.getAnnotation(Exposed.class).nameOverride();
                    StaticFunctionCache function = new StaticFunctionCache(method, ruleset , funcname);
                    if (functions.containsKey(funcname)){
                        Set<StaticFunctionCache> set = Set.of(functions.get(funcname), function);
                        packedFunctions.put(funcname, new PackedFunctionCache(funcname, new LinkedList<>(set)));
                        functions.remove(funcname);
                    }else {
                        if (packedFunctions.containsKey(funcname)){
                            packedFunctions.get(funcname).functions.add(function);
                        }else{
                            functions.put(funcname, function);
                        }
                    }
                }
            }
            cache.put(clazz, new LoaderCache(functions.values(), packedFunctions.values()));
        }catch (LoaderError loaderError){
            throw new CrashException(new CrashReport(loaderError.message, loaderError));
        }
        return true;
    }

    //added a APIRegistery object to the static list of API's to instanciate
    public static void register(APIRegistry registry){
        APIs.add(registry);
    }

    private void load(Runtime runtime, Computer computer){
        try{
            for (APIRegistry registry : APIs){
                if (registry.predicate(computer)){
                    API api = registry.Create(computer);
                    Function[] functions = translateAPI(api, runtime);
                    String label = api.getLabel();

                    Table apiTable = new Table();
                    for (Function func : functions){
                        if (func.getName()!=null){
                            apiTable.put(func.getName(), func.asValue());
                        } else {
                            throw new LoaderError("Issue encountered loading api '"+label+"': nameless function (try .setName on runtime implemented functions)");
                        }
                    }
                    api.postProcessing(apiTable);
                    ParentRuntime.getGlobals().put(label, apiTable.asValue());
                }
            }
        }catch (LoaderError loaderError){
            throw new CrashException(new CrashReport(loaderError.message, loaderError));
        }
    }

    private void loadIntoGlobals(Function[] functions, String label) throws LoaderError{
        Table apiTable = new Table();
        for (Function func : functions){
            if (func.getName()!=null){
                apiTable.put(func.getName(), func.asValue());
            }else{
                throw new LoaderError("Issue encountered loading api '"+label+"': nameless function (try .setName on runtime implemented functions)");
            }
        }
        ParentRuntime.getGlobals().put(label, apiTable.asValue());
    }

    public static Table TableizeAPI(Exposable exposable, @Nullable Runtime runtime){
        try{
            Function[] functions = translateAPI(exposable, runtime);
            Table apiTable = new Table();
            for (Function func : functions){
                if (func.getName()!=null){
                    apiTable.put(func.getName(), func.asValue());
                }else{
                    throw new LoaderError("Issue encountered loading api '"+exposable.getClass().getName()+"': nameless function (try .setName on runtime implemented functions)");
                }
            }
            return exposable.postProcessing(apiTable);
        }catch (LoaderError loaderError){
            throw new CrashException(new CrashReport(loaderError.message, loaderError));
        }
    }

    public static VarType interoperateParameterType(Class<?> clazz, boolean packingBias){
        if (packingBias){
            clazz = clazz.componentType();
            if (clazz==null) return VarType.NULL;
        }
        VarType type = VarType.NULL;
        if (clazz==String.class){
            type = VarType.STRING;
        }else if (clazz==int.class){
            type = VarType.NUMBER;
        }else if (clazz==double.class){
            type = VarType.NUMBER;
        }else if (clazz==float.class){
            type = VarType.NUMBER;
        }else if (clazz==boolean.class){
            type = VarType.BOOLEAN;
        }else if (clazz==Table.class){
            type = VarType.TABLE;
        }else if (clazz==Tuple.class){
            type = VarType.TUPLE;
        }else if (clazz==List.class){
            type = VarType.LIST;
        }else if (clazz==Function.class){
            type = VarType.FUNCTION;
        }else if (clazz==Bytes.class){
            type = VarType.BINARY;
        }else if (clazz==Value.class){
            type = VarType.ANY;
        }else if (clazz==Object.class){
            type = VarType.ANY;
        }
        return type;
    }

    //background methods used to process API's
    private static ParameterRules rulesFromMethod(Method method) throws LoaderError{
        boolean isPacked = method.isVarArgs();
        Parameter[] parameters = method.getParameters();
        ParameterRules rules = new ParameterRules();
        for (int i = 0; i < parameters.length; i++){
            if (i == parameters.length-1 && isPacked){
                rules.allowPacking(interoperateParameterType(parameters[i].getType(), true));
                if (parameters[i].isAnnotationPresent(Index.class)){
                    rules.packRule.giveIndexOffset(parameters[i].getAnnotation(Index.class).offset(), parameters[i].getAnnotation(Index.class).strict());
                }
                if (parameters[i].isAnnotationPresent(Range.class)){
                    rules.packRule.setRange(parameters[i].getAnnotation(Range.class).range());
                }
                rules.packRule.disName = parameters[i].getName();
            }else{
                rules.add(interoperateParameterType(parameters[i].getType(), false));
                if (parameters[i].isAnnotationPresent(Index.class)){
                    rules.rules.getLast().giveIndexOffset(parameters[i].getAnnotation(Index.class).offset(), parameters[i].getAnnotation(Index.class).strict());
                }
                if (parameters[i].isAnnotationPresent(Range.class)){
                    rules.rules.getLast().setRange(parameters[i].getAnnotation(Range.class).range());
                }
                rules.rules.getLast().disName = parameters[i].getName();
            }
        }
        for (ParameterRule rule : rules.rules){
            if (rule.type==VarType.NULL) throw new LoaderError('"'+method.getName()+'"'+" java function has unsupported parameter types");
        }
        if (rules.packRule!=null && rules.packRule.type==VarType.NULL) throw new LoaderError('"'+method.getName()+'"'+" java function has unsupported parameter types");
        return rules;
    }

    public static Object[] processArgs(Method method, FunctionInput input, @Nullable Context context){
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
                        int offset = parameters[i].isAnnotationPresent(Index.class) ? (context!=null ? (context.language.bumpIndexs() ? 1 : 0) : 0) + parameters[i].getAnnotation(Index.class).offset() : 0;
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
                }else if (parameters[i].getType()== Bytes.class){
                    List packed = input.getPacked();
                    Bytes[] pack = new Bytes[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        if (input.get(i).getValue() instanceof Bytes val) pack[d] = val;
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()== Value.class){
                    List packed = input.getPacked();
                    Value[] pack = new Value[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        pack[d] = input.get(i);
                    }
                    args[i] = pack;
                }else if (parameters[i].getType()== Object.class){
                    List packed = input.getPacked();
                    Object[] pack = new Object[packed.size()];
                    for (int d = 0; d < packed.size(); d++){
                        pack[d] = input.get(i).getValue();
                    }
                    args[i] = pack;
                }
            }else{
                if (parameters[i].getType()==String.class){
                    args[i] = input.get(i).toString();
                } else if (parameters[i].getType().toString().contains("Coerced")) {
                    if (input.get(i).toString() != null) { //make sure we dont try to turn wrong var into a string
                        args[i] = new Coerced<>(input.get(i).toString());
                    } else {
                        args[i] = new Coerced<>(input.get(i).getValue());
                    }
                } else if (parameters[i].getType()==int.class){
                    int offset = parameters[i].isAnnotationPresent(Index.class) ? (context!=null ? (context.language.bumpIndexs() ? 1 : 0) : 0) + parameters[i].getAnnotation(Index.class).offset() : 0;
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
                }else if (parameters[i].getType()==Bytes.class){
                    args[i] = input.get(i).toBytes();
                }else if (parameters[i].getType()==Value.class){
                    args[i] = input.get(i);
                }else if (parameters[i].getType()==Object.class){
                    args[i] = input.get(i).getValue();
                }
            }
        }
        return args;
    }

    public static void profilerFunction(long startTime, String function, @Nullable Context context){
        short timeSpent = (short) (System.currentTimeMillis() - startTime);
        if (timeSpent>100){
            profiler.warn(function +" exceeded 100 milliseconds ("+timeSpent+")");
            if (context!=null && context.runtime.getThread()!=null){
                context.runtime.getThread().taxJavaLag((short) 1000);
            }
        }
        if (context!=null && context.runtime.getThread()!=null){
            context.runtime.getThread().taxJavaLag(timeSpent);
        }
        if (timeSpent>10 && context!=null && context.runtime.getThread()!=null){
            context.runtime.getThread().yield();
        }
    }

    public record Context(Runtime runtime, LanguageGeneric language){}

    public static Function sandboxFunction(Method method, Object obj, ParameterRules ruleset, Runtime runtime){
        String funcname = method.isAnnotationPresent(Exposed.class) ? method.getAnnotation(Exposed.class).nameOverride().isBlank() ? method.getName() : method.getAnnotation(Exposed.class).nameOverride() : null;
        Function temp = new Function(false, ruleset) {
            @Override
            public Value call(FunctionInput parameters) {
                try {
                    if (runtime!=null && obj instanceof Exposable exposable){
                        if (runtime.isDead()) return Value.asError("Attempt to call function from killed runtime (how did you get here)");
                        exposable.onCall(runtime, method);
                    }
                    long timeStarted = System.currentTimeMillis();
                    Context context = runtime!=null ? new Context(runtime, NeetComputersServer.getLanguage(runtime.getThread().getLang())) : null;
                    Object retun = method.invoke(obj, processArgs(method, parameters, context));
                    if (context!=null) profilerFunction(timeStarted, funcname + ruleset.toString(context.runtime), context);
                    if (retun==null){
                        return Value.NULL;
                    }else{
                        return Value.of(retun);
                    }
                }catch (InvocationTargetException e){
                    if (e.getTargetException() instanceof ExposedError error) {
                        return Value.asError(error.getMessage());
                    }
                    printJavaError(e.getTargetException());
                    return Value.asError("Unexpected internal error, check log for information");
                }catch (java.lang.Exception e){
                    printJavaError(e);
                    return Value.asError("Unexpected internal error, check log for information");
                }
            }
        };
        temp.setName(funcname);
        return temp;
    }

    public static Function[] packFunctions(Hashtable<String, LinkedList<Function>> functions, Runtime runtime){
        Function[] output = new Function[functions.size()];
        AtomicInteger i = new AtomicInteger();
        functions.forEach((key, values) -> {
            if (values.size()==1){
                output[i.get()] = values.getFirst();
            }else{
                output[i.get()] = new Function(false, key, ParameterRules.ANY) {
                    @Override
                    public Value call(FunctionInput parameters) {
                        LinkedList<String> errors = new LinkedList<>();
                        LinkedList<String> names = new LinkedList<>();
                        for (Function function : values){
                            ParameterCheckReturn retur = ParameterRules.checkParameters(parameters.toArray(), function.getRules(), runtime);
                            if (!retur.isError()){
                                return function.invoke(retur.getFunctionInput());
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

    public static String generateJavaErrorLog(Throwable error){
        StringBuilder builder = new StringBuilder();
        int interations = 0;
        ArrayList<String> glossary = new ArrayList<>();
        Throwable buffer = error;
        while (buffer!=null) {
            /*write individual error and message*/
            builder.append(interations==0 ? '-' : '>');
            builder.append(':');
            builder.append(buffer.getClass().getName());
            if (buffer.getMessage()!=null){
                builder.append("\n  [ ");
                builder.append(buffer.getMessage());
                builder.append(" ]");
            }

            /*appends stack trace*/
            int stackNumber = 0;
            for (StackTraceElement trace : buffer.getStackTrace()){
                stackNumber++;
                builder.append("\n    ");
                builder.append(stackNumber);
                builder.append(": [");
                builder.append(trace.getLineNumber());
                builder.append("] ");
                builder.append(trace.getClassName());
                builder.append('.');
                builder.append(trace.getMethodName());
                if (trace.getFileName()!=null){
                    builder.append(" [");
                    builder.append(trace.getFileName());
                    if (trace.getLineNumber()>0){
                        builder.append(':');
                        builder.append(trace.getLineNumber());
                    }
                    builder.append(']');
                }
            }
            if (stackNumber==0) builder.append("\n    0: No Valid Stack Trace Available!");
            builder.append('\n');

            /*add class to glossary in reverse order to simplify printing later*/
            glossary.add(0,"  " + (interations + 1) + ':' + buffer.getClass().getName() + '\n');

            /*shift through the error stack*/
            interations++;
            buffer = buffer.getCause();
        }

        /*prepend glossary to string builder*/
        if (glossary.size()>1){
            String title = "Glossary:\n";
            builder.insert(0, new char[]{'\n','\n'}, 0 , 2);
            for (String entry : glossary){
                builder.insert(0, entry.toCharArray(), 0, entry.length());
            }
            builder.insert(0, title.toCharArray(), 0, title.length());
        }

        return "Stack Trace:\n" + builder;
    }

    public static void printJavaError(Throwable error){
        errorLogger.warn(generateJavaErrorLog(error), error);
    }

    private static Function packCachedFunction(PackedFunctionCache functionCache, Exposable obj, Runtime runtime){
        return new Function(false, functionCache.name, ParameterRules.ANY) {
            @Override
            public Value call(FunctionInput parameters) {
                LinkedList<String> errors = new LinkedList<>();
                LinkedList<String> names = new LinkedList<>();
                for (StaticFunctionCache staticFunction : functionCache.functions){
                    ParameterCheckReturn retur = ParameterRules.checkParameters(parameters.toArray(), staticFunction.ruleset(), runtime);
                    if (retur.isError()){
                        errors.add(retur.getMessage());
                        names.add(staticFunction.functionName() + staticFunction.ruleset().toString(runtime));
                    }else{
                        Function function = sandboxFunction(staticFunction.method(), obj, staticFunction.ruleset(), runtime);
                        return function.invoke(retur.getFunctionInput());
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

    public static String[] getFunctions(Exposable obj){
        Class<?> _class = obj.getClass();
        LinkedList<String> names = new LinkedList<>();
        Method[] buffer = _class.getMethods();
        for (Method method : buffer) {
            if (method.isAnnotationPresent(Exposed.class)) {
                Exposed annotation = method.getAnnotation(Exposed.class);
                if (annotation.nameOverride().isBlank()){
                    if (!names.contains(method.getName())) names.add(method.getName());
                }else{
                    if (!names.contains(annotation.nameOverride())) names.add(annotation.nameOverride());
                }
            }
        }
        names.sort(String::compareTo);
        return names.toArray(new String[0]);
    }

    public static String getName(Method method){
        if (method.isAnnotationPresent(Exposed.class)) {
            Exposed annotation = method.getAnnotation(Exposed.class);
            if (annotation.nameOverride().isBlank()){
                return method.getName();
            }else{
                return annotation.nameOverride();
            }
        }
        return "helpmeimtrapedinanightmareofmyowncreation";
    }

    public static Value<?> searchAndCall(Exposable obj, Runtime runtime, String name, Value<?>... args) throws LoaderError {
        Class<?> _class = obj.getClass();
        if (cache.containsKey(_class)) {
            LoaderCache cachedObject = cache.get(_class);
            for (StaticFunctionCache functionCache : cachedObject.functions()){
                if (functionCache.functionName().equals(name)){
                    ParameterCheckReturn retur = ParameterRules.checkParameters(args, functionCache.ruleset(), runtime);
                    if (!retur.isError()){
                        return sandboxFunction(functionCache.method(), obj, functionCache.ruleset(), runtime).invoke(retur.getFunctionInput());
                    }else{
                        return Value.asError(retur.getMessage());
                    }
                }
            }
            LinkedList<String> errors = new LinkedList<>();
            LinkedList<String> names = new LinkedList<>();
            for (PackedFunctionCache pFunctionCache : cachedObject.packedFunctions()){
                if (pFunctionCache.name.equals(name)){
                    for (StaticFunctionCache functionCache : pFunctionCache.functions){
                        ParameterCheckReturn retur = ParameterRules.checkParameters(args, functionCache.ruleset(), runtime);
                        if (!retur.isError()){
                            return sandboxFunction(functionCache.method(), obj, functionCache.ruleset(), runtime).invoke(retur.getFunctionInput());
                        }else{
                            errors.add(retur.getMessage());
                            names.add(name + functionCache.ruleset().toString(runtime));
                        }
                    }
                }
            }
            if (!errors.isEmpty()){
                names.sort(String::compareTo);
                StringBuilder error = new StringBuilder(errors.get(new Random().nextInt(errors.size())));
                for (String string : names){
                    error.append('\n');
                    error.append(string);
                }
                return new Exception(error.toString()).asValue();
            }else{
                return Value.asError("Cant Find Function '"+name+"'");
            }
        }else{
            Method[] buffer = _class.getMethods();
            LinkedList<String> errors = new LinkedList<>();
            LinkedList<String> names = new LinkedList<>();
            for (Method method : buffer){
                if (getName(method).equals(name)){
                    ParameterRules rules = rulesFromMethod(method);
                    ParameterCheckReturn retur = ParameterRules.checkParameters(args, rules, runtime);
                    if (!retur.isError()){
                        return sandboxFunction(method, obj, rules, runtime).invoke(retur.getFunctionInput());
                    }else{
                        errors.add(retur.getMessage());
                        names.add(name + rules.toString(runtime));
                    }
                }
            }
            if (errors.isEmpty()){
                return Value.asError("Cant Find Function '"+name+"'");
            }else if (errors.size()==1){
                return Value.asError(errors.getFirst());
            }else{
                names.sort(String::compareTo);
                StringBuilder error = new StringBuilder(errors.get(new Random().nextInt(errors.size())));
                for (String string : names){
                    error.append('\n');
                    error.append(string);
                }
                return new Exception(error.toString()).asValue();
            }
        }
    }

    public static Function[] translateAPI(Exposable obj, @Nullable Runtime runtime) throws LoaderError {
        if (!cache.containsKey(obj.getClass())) {
            Class<?> _class = obj.getClass();
            Hashtable<String, LinkedList<Function>> functions = new Hashtable<>();
            Method[] buffer = _class.getMethods();
            for (Method method : buffer) {
                if (method.isAnnotationPresent(Exposed.class)) {
                    ParameterRules ruleset = rulesFromMethod(method);
                    Function function = sandboxFunction(method, obj, ruleset, runtime);
                    if (functions.containsKey(function.getName())) {
                        functions.get(function.getName()).add(function);
                    } else {
                        functions.put(function.getName(), new LinkedList<>(Set.of(function)));
                    }
                }
            }
            return packFunctions(functions, runtime);
        }else{
            LoaderCache cachedLoader = cache.get(obj.getClass());
            LinkedList<Function> functions = new LinkedList<>();
            for (StaticFunctionCache staticFunctionCache : cachedLoader.functions()){
                functions.add(sandboxFunction(staticFunctionCache.method(), obj, staticFunctionCache.ruleset(), runtime));
            }
            for (PackedFunctionCache packedFunctionCache : cachedLoader.packedFunctions()){
                functions.add(packCachedFunction(packedFunctionCache, obj, runtime));
            }
            functions.sort(Comparator.comparing(Function::getName));
            return functions.toArray(new Function[]{});
        }
    }
}