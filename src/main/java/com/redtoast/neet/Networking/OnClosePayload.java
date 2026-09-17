package com.redtoast.neet.Networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record OnClosePayload(int syncID, UUID playerID) implements CustomPayload {
    public static final Id<OnClosePayload> ID = new Id<>(Identifier.of("neetcomputers", "onclosepayload"));
    public static final PacketCodec<RegistryByteBuf, OnClosePayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.syncID);
        buf.writeUuid(value.playerID);
    }, new PacketCodec<>() {
        @Override
        public OnClosePayload decode(RegistryByteBuf buf) {
            return new OnClosePayload(buf.readInt(), buf.readUuid());
        }

        @Override
        public void encode(RegistryByteBuf buf, OnClosePayload value) {
            buf.writeInt(value.syncID);
            buf.writeUuid(value.playerID);
        }
    });
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
