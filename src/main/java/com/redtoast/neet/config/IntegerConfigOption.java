package com.redtoast.neet.config;

public class IntegerConfigOption extends ConfigOption<Integer> {
    public IntegerConfigOption(Integer defaultOption, String optionName) {
        super(defaultOption, optionName);
    }

    @Override
    Integer parseFromString(String string) {
        return Integer.parseInt(string.trim());
    }
}
