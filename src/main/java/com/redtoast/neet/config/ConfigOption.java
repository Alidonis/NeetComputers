package com.redtoast.neet.config;

public abstract class ConfigOption<Type> {
    private final Type defaultOption;
    private final String optionName;
    public ConfigOption(Type defaultOption, String optionName){
        this.defaultOption = defaultOption;
        this.optionName = optionName;
    }
    abstract Type parseFromString(String string);
    public Type getDefaultOption() {return defaultOption;}
    public String getOptionName() {return optionName;}
}
