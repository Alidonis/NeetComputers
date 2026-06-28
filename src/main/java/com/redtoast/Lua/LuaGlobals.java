package com.redtoast.Lua;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.neet.config.ConfigLoader;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.FS.FileHelper;
import com.redtoast.simulation.FS.DiskSystem;
import com.redtoast.simulation.FS.FileImplementations.Filepath;
import com.redtoast.simulation.GlobalManager;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.GlobalGeneric;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterCheckReturn;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.value.VarType;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Objects;
import java.util.UUID;

public class LuaGlobals extends Globals implements GlobalGeneric {
    private final Logger logger;
    public LuaValue LuaDebug;
    protected static LuaTranslater lua52;
    private final UUID uuid;
    private final Hashtable<Value, Value> queue = new Hashtable<>();

    private static class NeoFinder implements ResourceFinder{
        private final DiskSystem fs;
        private int cursor = 0;
        public NeoFinder(DiskSystem fs){
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
        private final DiskSystem fs;
        private final Computer computer;
        public LuaRequire(LuaFunction require, DiskSystem fs, Runtime runtime){
            super(false, "require", new ParameterRules(VarType.STRING));
            this.computer = runtime.getParent();
            this.require = require;
            this.fs = fs;
        }
        @Override
        public Value call(FunctionInput parameters) {
            ParameterCheckReturn ckrn = ParameterRules.checkParameters(parameters.toArray(), new ParameterRules(VarType.STRING), computer.getRuntime());
            if (ckrn.isError()) return Value.asError(ckrn.getMessage());
            String path = parameters.get(0).toString();
            if (computer.libraryExists(path.toLowerCase())){
                return APILoader.TableizeAPI(computer.getLibrary(path.toLowerCase()), computer.getRuntime()).asValue();
            }else if (FileHelper.validatePathStatic(FileHelper.normalize(path))){
                Filepath filepath = fs.getFile(path+".lua");
                if (filepath.isInvalid()) throw new ExposedError("Invalid file path");
                if (!filepath.exists()) throw new ExposedError("No such file");
                if (!filepath.isFile()) throw new ExposedError("Not a file");
                if (!filepath.canRead()) throw new ExposedError("Access denied");
                LanguageTranslater translater = NeetComputersServer.getTranslater("Lua");
                assert translater != null;
                try{
                    Varargs args = require.call(LuaValue.valueOf(path));
                    return translater.toValue(args);
                }catch (Throwable ignored){
                    return Value.asError("Failed to load '"+path+".lua'");
                }
            }else{
                return Value.asError("Invalid asset path");
            }
        }
    }

    private static class LuaPrint extends Function{
        private final LuaGlobals globals;
        private final Logger logger;
        public LuaPrint(Runtime runtime, LuaGlobals globals, Logger logger) {
            super(false, ParameterRules.ANY);
            this.globals = globals;
            this.logger = logger;
        }

        @Override
        public Value call(FunctionInput parameters) {
            if (!(boolean) ConfigLoader.getServerConfig("print-to-console")) return null;
            LuaValue toStringFunc = globals.get("tostring");
            int iterator = 1;

            StringBuilder buffer = new StringBuilder();

            for(int var4 = parameters.getSize(); iterator <= var4; ++iterator) {
                if (iterator > 1) {
                    buffer.append('\t');
                }

                LuaString output = toStringFunc.call((LuaValue)lua52.fromValue(parameters.get(iterator-1))).strvalue();
                buffer.append(output.tojstring());
            }

            logger.info(buffer.toString());
            return null;
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
        logger = LoggerFactory.getLogger("Lua Runtime ["+globalManager.getParent().getParent().getUuid()+']');

        //fetch built in require object
        LuaFunction luaRequire = super.get("require").checkfunction();
        LuaDebug = super.get("debug");

        //get lang
        LanguageTranslater translater = NeetComputersServer.getTranslater("Lua");
        if (translater instanceof LuaTranslater luaTranslater) lua52 = luaTranslater;

        //remove unwanted base libs
        super.set("package", LuaValue.NIL);
        super.set("file",LuaValue.NIL);
        super.set("collectgarbage", LuaValue.NIL);
        super.set("_VERSION", LuaValue.NIL);

        //load new luaj resource finder
        super.finder = new NeoFinder(globalManager.getParent().getFileSpace().getHomeDisk());

        //set up new require functionality with anti-abuse in mind
        Varargs NewLuaRequire = lua52.fromValue(new LuaRequire(luaRequire, globalManager.getParent().getFileSpace().getHomeDisk(), globalManager.getParent()).asValue());
        Varargs NewPrint = lua52.fromValue(new LuaPrint(globalManager.getParent(), this, logger).asValue());
        assert NewLuaRequire instanceof LuaValue;
        super.set("require", (LuaValue) NewLuaRequire);
        super.set("print", (LuaValue) NewPrint);

        //register LuaGlobals with GlobalsManager
        uuid = UUID.randomUUID();

        //enable globals connection to GlobalManager
        globalManager.register(this);
        push();
    }

    //override Globals core set function to report back to manager object
    @Override
    public void rawset( LuaValue key, LuaValue value ) {
        super.rawset(key, value);
        if (Objects.equals(this, value)) return;
        //if (lua52!=null && !noForwarding && manager!=null) manager.put(uuid, lua52.toValue(key), lua52.toValue(value));
    }

    @Override
    public void insert(Value key, Value value) {
        queue.put(key, value);
    }

    public void push(){
        queue.forEach((key, value) -> {
            LuaValue luaKay = (LuaValue) lua52.fromValue(key.pack());
            super.set(luaKay, (LuaValue) lua52.fromValue(value.pack()));
        });
        queue.clear();
    }

    @Override
    public String getLang() {
        return "Lua";
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
