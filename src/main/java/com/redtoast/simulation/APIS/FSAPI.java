package com.redtoast.simulation.APIS;

import com.redtoast.simulation.LangAPI.*;
import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.LangAPI.Parameter.ParameterRules;
import org.luaj.vm2.LuaValue;

public class FSAPI extends LangAPI {
    public FSAPI(Runtime vm) {
        super("fs");

        CustomParameter ValidPath = new CustomParameter() {
            @Override
            public boolean rule(Value arg) {
                if (arg.instanceOf(VarType.STRING)){
                    return vm.files.validatePath((String) arg.getValue());
                }else{
                    return false;
                }
            }

            @Override
            public String getName() {
                return "Valid filepath (string)";
            }
        };

        set("isRootPath", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String path = (String) args.get(0).getValue();
                return new Value(vm.files.findRoot(path)!=null && vm.files.deObjectivify(path).equals(""));
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(ValidPath);
            }
        });
        set("isReadOnly", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String path = ((String) args.get(0).getValue()).replace('\\','/');
                if (!vm.files.exists(path)){
                    return Value.NULL;
                }
                if (path.equals("") || path.equals("/")){
                    return Value.TRUE;
                }
                return new Value(vm.files.findRoot(path).readOnly);
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(ValidPath);
            }
        });
        set("exists", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                return new Value(vm.files.exists((String) args.get(0).getValue()));
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(ValidPath);
            }
        });
        set("isDir", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String path = (String) args.get(0).getValue();
                if (!vm.files.exists(path)){
                    return Value.NULL;
                }
                return new Value(vm.files.isDir(path));
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(ValidPath);
            }
        });
        set("readAll", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String path = (String) args.get(0).getValue();
                if (!vm.files.exists(path)){
                    return Value.NULL;
                }
                return new Value(vm.files.readFile(path));
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(ValidPath);
            }
        });
        set("makeDir", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String path = (String) args.get(0).getValue();
                if (!vm.files.rootExists(path)){
                    return Value.NULL;
                }
                vm.files.makeDir(path);
                return Value.NULL;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(ValidPath);
            }
        });
        set("getFiles", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                String path = (String) args.get(0).getValue();
                if (!vm.files.exists(path)){
                    return Value.NULL;
                }
                if (!vm.files.isDir(path)){
                    return new Value("Path must be a directory");
                }
                String[] files = vm.files.getFiles(path);
                LuaValue[] values = new LuaValue[files.length];
                for (int i = 0; i < files.length; i++){
                    values[i] = LuaValue.valueOf(files[i]);
                }
                return new Value(values);
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(ValidPath);
            }
        });

    }
}
