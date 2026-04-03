package com.redtoast.neet.config;

public class BooleanConfigOption extends ConfigOption<Boolean>{
    public BooleanConfigOption(boolean defaultOption, String optionName) {
        super(defaultOption, optionName);
    }

    @Override
    Boolean parseFromString(String string) {
        string = string.trim();
        System.out.println(string);
        if (string.equalsIgnoreCase("true")) return true;
        if (string.equalsIgnoreCase("false")) return false;
        throw new RuntimeException();
    }
}
