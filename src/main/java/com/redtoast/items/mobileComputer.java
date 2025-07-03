package com.redtoast.items;

import com.redtoast.computerSpecs;
import com.redtoast.items.generics.ComputerItem;

public class mobileComputer extends ComputerItem {
    public mobileComputer(Settings settings) {
        super(settings, new computerSpecs()
                .setBinaryGraphicsSize(5, 6)
                .setColorGraphicsSize(108,192)
                .setIPS(130000)
                .setMaxCores(2)
                .setCoreUtilizationBonus(0)
                .setMachineName("Portable Computer")
        );
    }
}