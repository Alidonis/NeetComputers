package com.redtoast.neet.Networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClipboardRequest(String message) implements CustomPayload {
    public static final Id<ClipboardRequest> ID = new Id<>(Identifier.of("neetcomputers", "clipboard"));
    public static final PacketCodec<RegistryByteBuf, ClipboardRequest> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeString(value.message());
    }, new PacketCodec<>() {
        @Override
        public ClipboardRequest decode(RegistryByteBuf buf) {
            return new ClipboardRequest(buf.readString());
        }

        @Override
        public void encode(RegistryByteBuf buf, ClipboardRequest value) {
            buf.writeString(value.message());
        }
    });
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
