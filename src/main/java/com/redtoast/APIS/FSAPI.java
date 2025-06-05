package com.redtoast.APIS;

import com.redtoast.simulation.FileHandler;
import com.redtoast.simulation.annotations.CustomRule;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.value.LangError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.VarType;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.CustomParameter;
import com.redtoast.simulation.Runtime;

public class FSAPI implements API {
    Runtime runtime;

    @Override
    public String getLabel() {
        return "fs";
    }

    static class ValidPath extends CustomParameter {
        @Override
        public boolean rule(Value arg) {
            if (arg.instanceOf(VarType.STRING)){
                return FileHandler.validatePathStatic((String) arg.getValue());
            }else{
                return false;
            }
        }

        @Override
        public String getName() {
            return "Valid filepath";
        }
    }

    public FSAPI(Runtime vm) {
        runtime=vm;
    }

    @Exposed
    public Boolean isRootPath(@CustomRule(rule = ValidPath.class) String path){
        return runtime.files.findRoot(path)!=null && runtime.files.deObjectivify(path).equals("");
    }

    @Exposed
    public Boolean isReadOnly(@CustomRule(rule = ValidPath.class) String path){
        if (!runtime.files.exists(path)){
            return null;
        }
        if (path.equals("") || path.equals("/")){
            return true;
        }
        return runtime.files.findRoot(path).readOnly;
    }

    @Exposed
    public Boolean exists(@CustomRule(rule = ValidPath.class) String path){
        return runtime.files.exists(path);
    }

    @Exposed
    public Boolean isDir(@CustomRule(rule = ValidPath.class) String path){
        if (!runtime.files.exists(path)){
            return null;
        }
        return runtime.files.isDir(path);
    }

    @Exposed
    public String readAll(@CustomRule(rule = ValidPath.class) String path){
        if (!runtime.files.exists(path)){
            return null;
        }
        return runtime.files.readFile(path);
    }

    @Exposed
    public void makeDir(@CustomRule(rule = ValidPath.class) String path){
        if (!runtime.files.rootExists(path)){
            return;
        }
        runtime.files.makeDir(path);
    }

    @Exposed
    public List getFiles(@CustomRule(rule = ValidPath.class) String path){
        if (!runtime.files.exists(path)){
            return null;
        }
        if (!runtime.files.isDir(path)){
            throw new LangError("Path must be a directory");
        }
        String[] files = runtime.files.getFiles(path);
        Value[] values = new Value[files.length];
        for (int i = 0; i < files.length; i++){
            values[i] = new Value(files[i]);
        }
        return new List(values);
    }
}
