package com.redtoast.Lua;

import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.value.NVTable;
import org.luaj.vm2.*;

public class Lua_NV extends LuaTable{
    private final NVTable hashtable;
    private final LuaTranslater translater;
    public Lua_NV(NVTable NVtable) {
        super();
        LanguageTranslater genericTranslater = NeetComputers.getTranslater("Lua 5.2");
        assert genericTranslater instanceof LuaTranslater;
        translater = (LuaTranslater) genericTranslater;
        hashtable=NVtable;
    }

    @Override
    public void rawset(LuaValue key, LuaValue value) {
        if (key.isnil()) {
            hashtable.remove(key.tojstring());
            return;
        }
        hashtable.put(key.tojstring(), translater.toValue(value));
    }

    @Override
    public LuaValue rawget(LuaValue key){
        Varargs args = translater.fromValue(hashtable.get(key.tojstring()));
        assert args instanceof LuaValue;
        return (LuaValue) args;
    }
}
