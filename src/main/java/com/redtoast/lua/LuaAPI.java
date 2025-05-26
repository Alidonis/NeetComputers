package com.redtoast.lua;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.LinkedList;

public class LuaAPI {
    private final LuaTable table;
    private final String name;

    public LuaAPI(String n){
        name = n;
        table = new LuaTable();
    }
    public void insertSelf(Globals globals){
        if (name.equals("")){
            for (int i = 0; i < globals.narg(); i++){
                LuaValue k = LuaValue.NIL;
                while (true) {
                    Varargs n = table.next(k);
                    if ((k = n.arg1()).isnil()) break;
                    LuaValue v = n.arg(2);
                    globals.set(k, v);
                }
            }
        }else{
            globals.set(name,table);
        }
    }
    public void set(String n, LuaValue value){
        table.set(n, value);
    }
    public void set(String n, LuaFunction func){
        table.set(n, new LuaFuncClass(name+'.'+n,func));
    }
    public LuaValue get(String n){
        return table.get(n);
    }
    public LuaTable getTable(){
        return table;
    }
    public String getName(){
        return name;
    }
}
