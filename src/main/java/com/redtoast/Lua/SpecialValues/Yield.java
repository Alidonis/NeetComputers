package com.redtoast.Lua.SpecialValues;

import org.luaj.vm2.LuaNumber;

public class Yield extends LuaNumber {
    @Override
    public boolean equals(Object object){
        return object instanceof Yield;
    }
}
