package com.redtoast.neet.config;

public class RateConfigOption extends IntegerConfigOption {
    public RateConfigOption(Integer defaultOption, String optionName) {
        super(defaultOption, optionName);
    }

    @Override
    public Integer parseFromString(String string) {
        string = string.replaceFirst(" *[kK][bB]", "000");
        string = string.replaceFirst(" *[mM][bB]", "000000");
        string = string.replaceFirst(" *[gG][bB]", "000000000");
        string = string.replaceFirst(" *[tT][bB]", "000000000000");
        int buffer = super.parseFromString(string);
        if (buffer<0) throw new RuntimeException();
        return buffer;
    }
}
