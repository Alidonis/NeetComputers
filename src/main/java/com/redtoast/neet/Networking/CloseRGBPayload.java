package com.redtoast.neet.Networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CloseRGBPayload() implements CustomPayload {
    public static final Id<CloseRGBPayload> ID = new Id<>(Identifier.of("neetcomputers", "closergbpayload"));
    public static final PacketCodec<RegistryByteBuf, CloseRGBPayload> CODEC = PacketCodec.of((value, buf) -> {
    }, new PacketCodec<>() {
        @Override
        public CloseRGBPayload decode(RegistryByteBuf buf) {
            return new CloseRGBPayload();
        }

        @Override
        public void encode(RegistryByteBuf buf, CloseRGBPayload value) {
        }
    });
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
