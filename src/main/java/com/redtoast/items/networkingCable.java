package com.redtoast.items;

import com.redtoast.Connections.PipeType;
import com.redtoast.items.generics.ConnectorItem;

public class networkingCable extends ConnectorItem {
    public networkingCable(Settings settings) {
        super(settings, PipeType.NETWORK);
    }
}
