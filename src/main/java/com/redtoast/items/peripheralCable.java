package com.redtoast.items;

import com.redtoast.Connections.PipeType;
import com.redtoast.items.generics.ConnectorItem;

public class peripheralCable extends ConnectorItem {
    public peripheralCable(Settings settings) {
        super(settings, PipeType.PERIPHERAL);
    }
}
