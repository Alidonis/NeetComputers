package com.redtoast.lua;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.LinkedList;

public class LuaAPI {
    public LuaValue table;
    public String name;
    public interface LuaFunction {
        LuaValue main(LuaValue[] args);
        Rules getRules();
    }
    private static class LuaFuncClass extends VarArgFunction {
        LuaFunction lamb;
        String name;
        public LuaFuncClass(String n, LuaFunction lambda){
            name = n;
            lamb = lambda;
        }
        @Override
        public Varargs invoke(Varargs args) {
            LuaValue[] newargs = lamb.getRules().check(args,this);
            if (newargs.length==1){
                if (newargs[0].isclosure()){
                    return newargs[0];
                }
            }
            return lamb.main(newargs);
        }
    }
    public static class Rules{
        public interface Rule{boolean rule(LuaValue arg);}
        private final LinkedList<String> rules = new LinkedList<>();
        private final LinkedList<String> lambdaNames = new LinkedList<>();
        private final LinkedList<Rule> lambdaRules = new LinkedList<>();
        private boolean packed = false;
        private int optional = 0;
        public Rules(){}
        public Rules(String rule){
            rules.add(rule);
        }
        public Rules(Rule rule, String name){
            rules.add("Lambda "+(char)lambdaRules.size());
            lambdaNames.add(name);
            lambdaRules.add(rule);
        }
        public Rules(String rule, boolean o){
            rules.add(rule);
            if (o){
                optional++;
            }
        }
        public Rules(Rule rule, String name, boolean o){
            rules.add("Lambda "+(char)lambdaRules.size());
            lambdaNames.add(name);
            lambdaRules.add(rule);
            if (o){
                optional++;
            }
        }
        public Rules add(String rule){
            rules.add(rule);
            return this;
        }
        public Rules add(Rule rule, String name){
            rules.add("Lambda "+(char)lambdaRules.size());
            lambdaNames.add(name);
            lambdaRules.add(rule);
            return this;
        }
        public Rules add(String rule, boolean o){
            rules.add(rule);
            if (o){
                optional++;
            }
            return this;
        }
        public Rules add(Rule rule, String name, boolean o){
            rules.add("Lambda "+(char)lambdaRules.size());
            lambdaNames.add(name);
            lambdaRules.add(rule);
            if (o){
                optional++;
            }
            return this;
        }
        public Rules doPacking(){
            packed = true;
            return this;
        }
        public LuaValue[] check(Varargs args, LuaFuncClass func){
            if (!(args.narg()>=rules.size()-optional && (args.narg()<=rules.size() || packed))){
                return new LuaValue[]{LuaValue.error(func.name + " expected " + (rules.size() - optional) + " arguments, got " + args.narg())};
            }
            LuaValue[] values = new LuaValue[rules.size()];
            LuaValue[] pack = new LuaValue[args.narg() - rules.size()];
            int cap;
            if (packed){
                cap = args.narg();
            }else{
                cap = rules.size();
            }
            for (int i = 0; i < cap; i++){
                if (i<args.narg()){
                    if (rules.get(i).equalsIgnoreCase("any")){
                        values[i] = args.arg(i+1);
                    }else if(rules.get(i).charAt(0)=='L'){
                        if (lambdaRules.get((int)rules.get(i).charAt(7)).rule(args.arg(i+1))){
                            values[i] = args.arg(i+1);
                        }else{
                            return new LuaValue[]{LuaValue.error(func.name + " expected " + lambdaNames.get((int) rules.get(i).charAt(7)) + ", got " + args.arg(i+1).typename())};
                        }
                    }else{
                        if (args.arg(i).typename().equalsIgnoreCase(rules.get(i))){
                            values[i] = args.arg(i+1);
                        }else{
                            return new LuaValue[]{LuaValue.error(func.name + " expected " + rules.get(i) + ", got " + args.arg(i+1).typename())};
                        }
                    }
                }else if (packed && i >= rules.size()) {
                    if (rules.get(rules.size()-1).equalsIgnoreCase("any")){
                        pack[rules.size()-i] = args.arg(i+1);
                    }else if(rules.get(rules.size()-1).charAt(0)=='L'){
                        if (lambdaRules.get((int)rules.get(rules.size()-1).charAt(7)).rule(args.arg(i+1))){
                            pack[rules.size()-i] = args.arg(i+1);
                        }else{
                            return new LuaValue[]{LuaValue.error(func.name + " expected " + lambdaNames.get((int) rules.get(i).charAt(7)) + ", got " + args.arg(i+1).typename())};
                        }
                    }else{
                        if (args.arg(i).typename().equalsIgnoreCase(rules.get(rules.size()-1))){
                            pack[rules.size()-i] = args.arg(i+1);
                        }else{
                            return new LuaValue[]{LuaValue.error(func.name + " expected " + rules.get(i) + ", got " + args.arg(i+1).typename())};
                        }
                    }
                }else{
                    values[i] = LuaValue.NIL;
                }
            }
            if (packed){
                values[cap-1] = (LuaValue) LuaValue.varargsOf(pack);
            }
            return values;
        }
    }
    public LuaAPI(String n){
        name = n;
        table = new LuaTable();
    }
    public void register(String n, LuaFunction func){
        table.set(n, new LuaFuncClass(name+'.'+n,func));
    }
}
