package com.redtoast.neet.config;

import dan200.computercraft.api.lua.IComputerSystem;

public class CompatibilityConfigOption extends BooleanConfigOption {
    public CompatibilityConfigOption(boolean defaultOption, String optionName) {
        super(defaultOption, optionName);
    }

    @Override
    public Boolean parseFromString(String string) {
        boolean CCExists = false;
        try {
            Class<IComputerSystem> ignored = IComputerSystem.class;
            CCExists = true;
        }catch (NoClassDefFoundError ignored){}
        return super.parseFromString(string) && CCExists;
    }

    @Override
    public Boolean getDefaultOption(){
        boolean CCExists = false;
        try {
            Class<IComputerSystem> ignored = IComputerSystem.class;
            CCExists = true;
        }catch (NoClassDefFoundError ignored){}
        return super.getDefaultOption() && CCExists;
    }
}
