package com.redtoast.Lua;

import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.FS.FileHelper;
import com.redtoast.simulation.FS.FileSpace;
import com.redtoast.simulation.FS.Filepath;
import com.redtoast.simulation.GlobalManager;
import com.redtoast.simulation.base.GlobalGeneric;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.value.ValueTypes.Table;
import com.redtoast.simulation.value.VarType;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.UUID;

public class LuaGlobals extends Globals implements GlobalGeneric {
    private final LuaFunction LuaRequire;
    public LuaValue LuaDebug;
    protected LuaTranslater lua52;
    private final UUID uuid;
    private final GlobalManager manager;
    private boolean noForwarding = false;
    private final Hashtable<Value, Value> queue = new Hashtable<>();

    private static class NeoFinder implements ResourceFinder{
        private final FileSpace fs;
        private int cursor = 0;
        public NeoFinder(FileSpace fs){
            this.fs = fs;
        }
        @Override
        public InputStream findResource(String s) {
            Filepath file = fs.getFile(s);
            if (file.isInvalid()) return null;
            if (!file.exists()) return null;
            if (!file.isFile()) return null;
            if (!file.canRead()) return null;
            try{
                byte[] out = file.readAll().getBytes();
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        if (cursor==out.length){
                            return -1;
                        }else{
                            cursor++;
                            return out[cursor-1] & 0xFF;
                        }
                    }
                };
            }catch (Throwable ignored){
                return null;
            }
        }
    }

    private static class LuaRequire extends Function{
        private final LuaFunction require;
        private final FileSpace fs;
        public LuaRequire(LuaFunction require, FileSpace fs){
            super("require", new ParameterRules(VarType.STRING));
            this.require = require;
            this.fs = fs;
        }
        @Override
        public Value call(FunctionInput parameters) {
            String path = parameters.get(0).toString();
            assert path != null;
            if (FileHelper.validatePathStatic(FileHelper.normalize(path))){
                Filepath filepath = fs.getFile(path+".lua");
                if (filepath.isInvalid()) throw new ExposedError("Invalid file path");
                if (!filepath.exists()) throw new ExposedError("No such file");
                if (!filepath.isFile()) throw new ExposedError("Not a file");
                if (!filepath.canRead()) throw new ExposedError("Access denied");
                LanguageTranslater translater = NeetComputers.getTranslater("Lua 5.2");
                assert translater != null;
                try{
                    return translater.toValue(require.call(LuaValue.valueOf(path)));
                }catch (Throwable ignored){
                    return Value.asError("Failed to load '"+path+".lua'");
                }
            }else{
                return Value.asError("Invalid file path");
            }
        }
    }

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

        //fetch built in require object
        LuaRequire = super.get("require").checkfunction();
        LuaDebug = super.get("debug");

        //get lang
        LanguageTranslater translater = NeetComputers.getTranslater("Lua 5.2");
        if (translater instanceof LuaTranslater luaTranslater) lua52 = luaTranslater;
        manager = globalManager;

        //remove unwanted base libs
        Table debug = APILoader.TableizeAPI(new DebugWrapper(LuaDebug, this), globalManager.getParent());
        Varargs debugArgs = lua52.fromValue(debug.asValue());
        assert debugArgs instanceof LuaValue;
        super.set("package", LuaValue.NIL);
        //super.set("debug", (LuaValue) debugArgs);
        super.set("file",LuaValue.NIL);
        super.set("dofile",LuaValue.NIL);
        super.set("loadfile",LuaValue.NIL);
        super.set("collectgarbage", LuaValue.NIL);
        super.set("_VERSION", LuaValue.NIL);

        //load new luaj resource finder
        super.finder = new NeoFinder(globalManager.getParent().fs);

        //set up new require functionality with anti-abuse in mind
        Varargs NewLuaRequire = lua52.fromValue(new LuaRequire(LuaRequire, manager.getParent().fs).asValue());
        assert NewLuaRequire instanceof LuaValue;
        super.set("require", (LuaValue) NewLuaRequire);

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
