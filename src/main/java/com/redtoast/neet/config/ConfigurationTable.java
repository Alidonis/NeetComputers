package com.redtoast.neet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class ConfigurationTable {
    private final Hashtable<String, ConfigOption<?>> optionTable = new Hashtable<>();
    private final Hashtable<String, Object> valueTable = new Hashtable<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("NeetComputers config");

    public ConfigurationTable(ConfigOption<?>[] options) {
        for (ConfigOption<?> configOption : options) {
            String key = configOption.getOptionName().toLowerCase();
            optionTable.put(key, configOption);
            valueTable.put(key, configOption.getDefaultOption());
        }
    }

    public void reset() {
        optionTable.forEach((key, value) -> {
            valueTable.put(key, value.getDefaultOption());
        });
    }

    public void attemptPush(String key, String value) {
        String refinedKey = key.trim().toLowerCase();
        if (optionTable.containsKey(refinedKey)) {
            try {
                Object obj = optionTable.get(refinedKey).parseFromString(value);
                valueTable.put(refinedKey, obj);
            }catch (Exception ignored) {
                LOGGER.warn("NEET computers config error, {} isn't a valid option for setting '{}'", value, refinedKey);
            }
        }else{
            LOGGER.warn("NEET computers config error, cant find setting '{}'", refinedKey);
        }
    }

    public Object getSetting(String key) {
        return valueTable.get(key);
    }
}
