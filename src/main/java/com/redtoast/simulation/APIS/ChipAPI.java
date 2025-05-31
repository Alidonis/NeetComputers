package com.redtoast.simulation.APIS;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.LangAPI.LangAPI;
import com.redtoast.simulation.LangAPI.LambdaFunction;
import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.Parameter.ParameterRules;
import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.ValueTypes.Function;
import com.redtoast.simulation.LangAPI.VarType;
import com.redtoast.simulation.LangThread;
import com.redtoast.simulation.Runtime;

import java.util.LinkedList;
import java.util.UUID;

public abstract class ChipAPI extends LangAPI {
    Computer computer;
    Runtime vm;

    public abstract LinkedList<LangThread> getThreads();
    public abstract LangThread getThread();
    public abstract void addThread(LangThread thread);
    public abstract void registerEvent(String event, Function func);

    public ChipAPI(Computer parent, Runtime VM, int maxThreadCount) {
        super("chip");
        computer = parent;
        vm = VM;

        set("getTime", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                return new Value(System.currentTimeMillis());
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });

        set("getUUID", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                return new Value(computer.getUuid().toString());
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });

        set("getThreadCount", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                return new Value(getThreads().size());
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });

        set("getMaxThreadCount", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                return new Value(maxThreadCount);
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });

        set("createThread", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                if (getThreads().size()>=maxThreadCount) return Value.asError("Thread cap for this machine reached, cant make more threads");
                String text = (String) args.get(0).getValue();
                UUID uuid = vm.MakeThread(text, "Lua 5.2");
                return new Value(uuid.toString());
            }

            @Override
            public ParameterRules getRules() {
                ParameterRules rule = new ParameterRules(VarType.STRING);
                return rule;
            }
        });

        set("getCurrentThread", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                UUID uuid = getThread().getUuid();
                return new Value(uuid.toString());
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });

        set("killThread", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                LinkedList<LangThread> threads = getThreads();
                for (LangThread thread : threads){
                    if (thread.getUuid().toString().equals(args.get(0).getValue())){
                        thread.kill();
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



        set("shutdown", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                computer.Stop();
                return Value.NULL;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });

        set("version", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                return new Value<>(NeetComputers.version);
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });
    }
}
