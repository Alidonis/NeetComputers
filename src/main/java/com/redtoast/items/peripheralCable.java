package com.redtoast.items;

import com.redtoast.Connections.PipeType;

public class peripheralCable extends ConnectorItem {
    public peripheralCable(Settings settings) {
        super(settings, PipeType.PERIPHERAL);
    }
}
