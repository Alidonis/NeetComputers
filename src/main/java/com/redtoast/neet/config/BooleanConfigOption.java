package com.redtoast.neet.config;

public class BooleanConfigOption extends ConfigOption<Boolean>{
    public BooleanConfigOption(boolean defaultOption, String optionName) {
        super(defaultOption, optionName);
    }

    @Override
    Boolean parseFromString(String string) {
        return Boolean.parseBoolean(string.trim());
    }
}
