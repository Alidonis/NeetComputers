package com.redtoast.Lua;

import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.Value;
import org.luaj.vm2.LuaValue;

public class DebugWrapper implements API {
    LuaValue debug;
    LuaGlobals globals;

    public DebugWrapper(LuaValue debug, LuaGlobals globals){
        this.debug = debug;
        this.globals = globals;
    }

    @Exposed
    public Value debug(Value... args){
        return globals.lua52.toValue(debug.get("debug").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value getfenv(Value... args){
        return globals.lua52.toValue(debug.get("getfenv").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value gethook(Value... args){
        throw new ExposedError("Debug.gethook is disabled for N.E.E.T. computers V"+ NeetComputers.version);
        //return globals.lua52.toValue(debug.get("gethook").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value getinfo(Value... args){
        return globals.lua52.toValue(debug.get("getinfo").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value getlocal(Value... args){
        return globals.lua52.toValue(debug.get("getlocal").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value getmetatable(Value... args){
        throw new ExposedError("Debug.getmetatable is disabled for N.E.E.T. computers V"+ NeetComputers.version);
        //return globals.lua52.toValue(debug.get("getmetatable").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value getregistry(Value... args){
        return globals.lua52.toValue(debug.get("getregistry").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value getupvalue(Value... args){
        return globals.lua52.toValue(debug.get("getupvalue").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value setfenv(Value... args){
        return globals.lua52.toValue(debug.get("setfenv").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value sethook(Value... args){
        return globals.lua52.toValue(debug.get("sethook").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value setlocal(Value... args){
        return globals.lua52.toValue(debug.get("setlocal").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value setmetatable(Value... args){
        throw new ExposedError("Debug.setmetatable is disabled for N.E.E.T. computers V"+ NeetComputers.version);
        //return globals.lua52.toValue(debug.get("setmetatable").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value setupvalue(Value... args){
        return globals.lua52.toValue(debug.get("setupvalue").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value traceback(Value... args){
        return globals.lua52.toValue(debug.get("traceback").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value upvalueid(Value... args){
        return globals.lua52.toValue(debug.get("upvalueid").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Exposed
    public Value upvaluejoin(Value... args){
        return globals.lua52.toValue(debug.get("upvaluejoin").invoke(globals.lua52.fromValue(Value.of(args).toTuple().asValue())));
    }

    @Override
    public String getLabel() {
        return "debug";
    }
}
