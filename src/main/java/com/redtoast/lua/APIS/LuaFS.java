package com.redtoast.lua.APIS;

import com.redtoast.lua.LuaAPI;
import com.redtoast.lua.LuaVM;
import org.luaj.vm2.LuaValue;

public class LuaFS extends LuaAPI {
    public LuaFS(LuaVM vm) {
        super("fs");

        Rules.Rule ValidPath = arg -> {
            if (arg.isstring()){
                return vm.files.validatePath(arg.toString());
            }else{
                return false;
            }
        };

        set("isRootPath", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {

                String path = args[0].toString();
                return LuaValue.valueOf(vm.files.findRoot(path)!=null && vm.files.deObjectivify(path).equals(""));
            }

            @Override
            public Rules getRules() {
                return new Rules(ValidPath,"valid path");
            }
        });
        set("isReadOnly", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                String path = args[0].toString().replace('\\','/');
                if (!vm.files.exists(path)){
                    return LuaValue.NIL;
                }
                if (path.equals("") || path.equals("/")){
                    return LuaValue.TRUE;
                }
                return LuaValue.valueOf(vm.files.findRoot(path).readOnly);
            }

            @Override
            public Rules getRules() {
                return new Rules(ValidPath,"valid path");
            }
        });
        set("exists", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                return LuaValue.valueOf(vm.files.exists(args[0].toString()));
            }

            @Override
            public Rules getRules() {
                return new Rules(ValidPath,"valid path");
            }
        });
        set("isDir", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                String path = args[0].toString();
                if (!vm.files.exists(path)){
                    return LuaValue.NIL;
                }
                return LuaValue.valueOf(vm.files.isDir(path));
            }

            @Override
            public Rules getRules() {
                return new Rules(ValidPath,"valid path");
            }
        });
        set("readAll", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                String path = args[0].toString();
                if (!vm.files.exists(path)){
                    return LuaValue.NIL;
                }
                return LuaValue.valueOf(vm.files.readFile(path));
            }

            @Override
            public Rules getRules() {
                return new Rules(ValidPath,"valid path");
            }
        });
        set("makeDir", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                String path = args[0].toString();
                if (!vm.files.rootExists(path)){
                    return LuaValue.NIL;
                }
                vm.files.makeDir(path);
                return LuaValue.NIL;
            }

            @Override
            public Rules getRules() {
                return new Rules(ValidPath,"valid path");
            }
        });
        set("getFiles", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                String path = args[0].toString();
                if (!vm.files.exists(path)){
                    return LuaValue.NIL;
                }
                if (!vm.files.isDir(path)){
                    return LuaValue.error("Path must be a directory");
                }
                String[] files = vm.files.getFiles(path);
                LuaValue[] values = new LuaValue[files.length];
                for (int i = 0; i < files.length; i++){
                    values[i] = LuaValue.valueOf(files[i]);
                }
                return LuaValue.listOf(values);
            }

            @Override
            public Rules getRules() {
                return new Rules(ValidPath,"valid path");
            }
        });

    }
}
