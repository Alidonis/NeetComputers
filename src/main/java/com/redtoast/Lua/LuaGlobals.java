package com.redtoast.Lua;

import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.GlobalManager;
import com.redtoast.simulation.base.GlobalGeneric;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.value.Value;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

import java.util.Hashtable;
import java.util.UUID;

public class LuaGlobals extends Globals implements GlobalGeneric {
    public final LuaValue LuaDebug;
    private LuaTranslater lua52;
    private GlobalManager globals;
    private final UUID uuid;
    private final GlobalManager manager;
    private boolean noForwarding = false;
    private final Hashtable<Value, Value> queue = new Hashtable<>();

    public LuaGlobals(GlobalManager globalManager){
        //generate Globals based off how jsePlatform.debugGlobals() works without a few unnecessary library's
        super();
        super.load(new JseBaseLib());
        super.load(new PackageLib());
        super.load(new Bit32Lib());
        super.load(new TableLib());
        super.load(new StringLib());
        super.load(new CoroutineLib());
        super.load(new JseMathLib());
        LoadState.install(this);
        LuaC.install(this);
        super.load(new DebugLib());

        //fetch some library's we need form the globals object
        LuaDebug = super.get("debug");

        //remove unwanted base libs
        super.set("package",LuaValue.NIL);
        super.set("require",LuaValue.NIL);
        super.set("debug",LuaValue.NIL);
        super.set("file",LuaValue.NIL);
        super.set("load",LuaValue.NIL);
        super.set("dofile",LuaValue.NIL);
        super.set("loadfile",LuaValue.NIL);

        //get lang
        LanguageTranslater translater = NeetComputers.getTranslater("Lua 5.2");
        if (translater instanceof LuaTranslater luaTranslater) lua52 = luaTranslater;
        manager = globalManager;

        //register LuaGlobals with GlobalsManager
        uuid = UUID.randomUUID();

        //enable globals connection to GlobalManager
        manager.register(this);
        push();
    }

    //override Globals core set function to report back to manager object
    @Override
    public void rawset( LuaValue key, LuaValue value ) {
        super.rawset(key, value);
        if (lua52!=null && !noForwarding) manager.put(uuid, lua52.toValue(key), lua52.toValue(value));
    }

    @Override
    public void insert(Value key, Value value) {
        queue.put(key, value);
    }

    public void push(){
        noForwarding = true;
        queue.forEach((key, value) -> {
            LuaValue luaKay = (LuaValue) lua52.fromValue(key.pack());
            super.set(luaKay, (LuaValue) lua52.fromValue(value.pack()));
        });
        noForwarding = false;
        queue.clear();
    }

    @Override
    public String getLang() {
        return "Lua 5.2";
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
