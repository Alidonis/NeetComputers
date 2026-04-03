package com.redtoast.neet.config;

import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class ConfigLoader {
    private static final ConfigurationTable serverConfigurationTable = new ConfigurationTable(new ConfigOption[]{
            new IntegerConfigOption(1, "processing-threads"),
            new BooleanConfigOption(true, "computers-emit-light"),
            new BooleanConfigOption(false, "experimental-compatibility"),
            new BooleanConfigOption(false, "print-to-console"),
            new BooleanConfigOption(true, "shift-click-to-clear-displays")
    });
    private static final ConfigurationTable clientConfigurationTable = new ConfigurationTable(new ConfigOption[]{
    });

    public static Object getServerConfig(String key) {
        return serverConfigurationTable.getSetting(key);
    }

    public static void loadServerConfig(MinecraftServer server) {
        File config = server.getPath("config").resolve("NEET-server.properties").toFile();
        serverConfigurationTable.reset();
        if (config.exists()){
            try {
                Scanner scanner = new Scanner(config);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (!line.isEmpty() && line.charAt(0) != '#') {
                        String[] parts = line.split("=");
                        if (parts.length == 2) {
                            serverConfigurationTable.attemptPush(parts[0], parts[1]);
                        }
                    }
                }
                scanner.close();
            }catch (Exception ignored){}
        }else{
            try {
                config.createNewFile();
                FileWriter writer = new FileWriter(config, false);
                writer.write("""
                        #allows players to shift click peripheral displays to clear them
                        shift-click-to-clear-displays = true
                        #when true, active computers will emit faint light
                        computers-emit-light = true
                        #determines how many threads the server will create to manage computing, more threads is optimal on CPU's with extra cores to spare
                        processing-threads = 1
                        #allows the default core print function to echo to the game console, meant for debugging
                        print-to-console = false
                        #allows experimental compatibility features
                        experimental-compatibility = false
                        """);
                writer.close();
            }catch (Exception ignored){}
        }
    }
}
