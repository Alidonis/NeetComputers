package com.redtoast.neet.Networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SubmitCommandPayload(String command, int syncId) implements CustomPayload {
    public static final Id<SubmitCommandPayload> ID = new Id<>(Identifier.of("neetcomputers", "submitcommandpayload"));
    public static final PacketCodec<RegistryByteBuf, SubmitCommandPayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeString(value.command());
        buf.writeInt(value.syncId());
    }, new PacketCodec<>() {
        @Override
        public SubmitCommandPayload decode(RegistryByteBuf buf) {
            return new SubmitCommandPayload(buf.readString(), buf.readInt());
        }

        @Override
        public void encode(RegistryByteBuf buf, SubmitCommandPayload value) {
            buf.writeString(value.command());
            buf.writeInt(value.syncId());
        }
    });
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
