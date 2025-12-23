package com.redtoast.Lua;

import com.redtoast.neet.NeetComputersServer;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Table;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class LiveTable extends LuaTable {
    private final Table table;
    private final LuaTranslater translater;
    public LiveTable(Table table){
        super();
        this.table = table;
        LanguageTranslater genericTranslater = NeetComputersServer.getTranslater("Lua 5.2");
        assert genericTranslater instanceof LuaTranslater;
        translater = (LuaTranslater) genericTranslater;
    }

    @Override
    public LuaValue rawget(LuaValue value){
        Value value1 = this.table.get(translater.toValue(value));
        if (value1==null) return LuaValue.NIL;
        Varargs args = translater.fromValue(value1);
        if (args instanceof LuaValue val){
            return val;
        }else{
            return LuaValue.NIL;
        }
    }

    @Override
    public void rawset(LuaValue value1, LuaValue value2){
        table.put(translater.toValue(value1), translater.toValue(value2));
    }

    @Override
    public LuaValue getmetatable(){
        if (table.getMetaTable()==null) return new LuaTable();
        assert table.getMetaTable() != null;
        Varargs args = translater.fromValue(table.getMetaTable().asValue());
        if (args instanceof LuaValue value){
            return value;
        }else{
            return new LuaTable();
        }
    }

    @Override
    public LuaValue setmetatable(LuaValue value){
        table.setMetaTable(translater.toValue(value).toTable());
        return this;
    }
}