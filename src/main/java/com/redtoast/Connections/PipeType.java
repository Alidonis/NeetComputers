package com.redtoast.Connections;

import net.minecraft.util.Identifier;

public enum PipeType {
    PERIPHERAL(Identifier.of("neetcomputers", "block/connections/peripheral_pipe"), Identifier.of("neetcomputers", "block/connections/peripheral_source")),
    NETWORK(Identifier.of("neetcomputers", "block/connections/network_pipe"), Identifier.of("neetcomputers", "block/connections/network_source"));

    private final Identifier texture;
    private final Identifier sourceTexture;

    PipeType(Identifier texture, Identifier sourceTexture) {
        this.texture = texture;
        this.sourceTexture = sourceTexture;
    }

    public Identifier getTexture(){
        return texture;
    }
    public Identifier getSourceTexture(){
        return sourceTexture;
    }
}
