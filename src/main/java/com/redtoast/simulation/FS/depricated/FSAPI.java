package com.redtoast.simulation.FS.depricated;

@Deprecated
public class FSAPI{// implements API {
//    Runtime runtime;
//
//    @Override
//    public String getLabel() {
//        return "fs";
//    }
//
//    @CustomRule(rule = "ValidPath")
//    static class ValidPath extends CustomParameter {
//        @Override
//        public boolean rule(Value arg) {
//            if (arg.instanceOf(VarType.STRING)){
//                return FileHandler.validatePathStatic((String) arg.getValue());
//            }else{
//                return false;
//            }
//        }
//
//        @Override
//        public String getName() {
//            return "Valid filepath";
//        }
//    }
//
//    public FSAPI(Computer vm) {
//        runtime=vm.getRuntime();
//    }
//
//    @Exposed
//    public Boolean isRootPath(@CustomRule(rule = "ValidPath") String path){
//        return runtime.files.findRoot(path)!=null && runtime.files.deObjectivify(path).equals("");
//    }
//
//    @Exposed
//    public Boolean isReadOnly(@CustomRule(rule = "ValidPath") String path){
//        if (!runtime.files.exists(path)){
//            return null;
//        }
//        if (path.equals("") || path.equals("/")){
//            return true;
//        }
//        return runtime.files.findRoot(path).readOnly;
//    }
//
//    @Exposed
//    public Boolean exists(@CustomRule(rule = "ValidPath") String path){
//        return runtime.files.exists(path);
//    }
//
//    @Exposed
//    public Boolean isDir(@CustomRule(rule = "ValidPath") String path){
//        if (!runtime.files.exists(path)){
//            return null;
//        }
//        return runtime.files.isDir(path);
//    }
//
//    @Exposed
//    public String readAll(@CustomRule(rule = "ValidPath") String path){
//        if (!runtime.files.exists(path)){
//            return null;
//        }
//        return runtime.files.readFile(path);
//    }
//
//    @Exposed
//    public void makeDir(@CustomRule(rule = "ValidPath") String path){
//        if (!runtime.files.rootExists(path)){
//            return;
//        }
//        runtime.files.makeDir(path);
//    }
//
//    @Exposed
//    public List getFiles(@CustomRule(rule = "ValidPath") String path){
//        if (!runtime.files.exists(path)){
//            return null;
//        }
//        if (!runtime.files.isDir(path)){
//            throw new LangError("Path must be a directory");
//        }
//        String[] files = runtime.files.getFiles(path);
//        Value[] values = new Value[files.length];
//        for (int i = 0; i < files.length; i++){
//            values[i] = Value.of(files[i]);
//        }
//        return new List(values);
//    }
}
