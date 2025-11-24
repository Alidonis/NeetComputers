package com.redtoast.Connections;

import net.minecraft.util.Identifier;

public enum PipeType {
    PERIPHERAL(Identifier.of("neetcomputers", "block/peripheral_pipe"));

    private final Identifier identifier;

    PipeType(Identifier texture) {
        this.identifier = texture;
    }

    public Identifier getTexture(){
        return identifier;
    }
}
