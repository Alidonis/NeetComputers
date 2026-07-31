package com.redtoast.YSLua;

import com.redtoast.neet.NeetComputersServer;
import com.redtoast.neet.config.ConfigLoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.config.ComputerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuaThread extends LangThread {
    private final Logger logger;
    private final LuaBridge bridge;

    public LuaThread(String script, Runtime parentRuntime, ComputerConfig computerConfig) {
        logger = LoggerFactory.getLogger("Lua Runtime [" + parentRuntime.getParent().getUuid() + ']');
        LuaThread self = this;
        bridge = new LuaBridge() {
            @Override
            public void print(String text) {
                if (!(boolean) ConfigLoader.getServerConfig("print-to-console")) return;
                logger.info(text);
            }

            @Override
            public void error(String error) {
                self.error(error.replaceFirst("^\\[String \"Lua\"]", "Lua"));
                kill(error.replaceFirst("^\\[String \"Lua\"]", "Lua"));
                close();
            }

            @Override
            public void shutDown() {
                kill();
                close();
            }

            @Override
            public void log(String message) {
                self.log(message);
            }

            @Override
            public boolean isAlive() {
                if (parentRuntime.getParent().isCrashed()) {
                    shutDown();
                } else if (parentRuntime.shouldDie()) {
                    shutDown();
                    parentRuntime.getParent().stop();
                }
                return self.isAlive();
            }
        };
        LuaTranslater translater = (LuaTranslater) NeetComputersServer.getTranslater("Lua");
        assert translater != null;
        parentRuntime.loader.load(parentRuntime.getParent(), (key, value) -> bridge.setGlobal(key.getValue().toString(), translater.fromValue(value)));

        bridge.setGlobal("bit32", Bit32Compat.build());

        bridge.init(script, computerConfig.instructionsPerBatch(), computerConfig.batchesPerTick());
    }

    @Override
    public String getLang() {
        return "Lua";
    }

    @Override
    public void yield() {
        bridge.yield();
    }

    @Override
    public void tick() {
        bridge.tick();
    }

    @Override
    public String getSource() {
        return "[Lua]";
    }
}
