package com.redtoast.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

class LuaFuncClass extends VarArgFunction {
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