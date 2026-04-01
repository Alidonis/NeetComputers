package com.redtoast.neet.Networking;

import com.redtoast.simulation.value.VarType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReturnMessagePayload(String message, VarType type) implements CustomPayload {
    public static final Id<ReturnMessagePayload> ID = new Id<>(Identifier.of("neetcomputers", "returnmessagepayload"));
    public static final PacketCodec<RegistryByteBuf, ReturnMessagePayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeString(value.message());
        buf.writeInt(value.type().ordinal());
    }, new PacketCodec<>() {
        @Override
        public ReturnMessagePayload decode(RegistryByteBuf buf) {
            return new ReturnMessagePayload(buf.readString(), VarType.values()[buf.readInt()]);
        }

        @Override
        public void encode(RegistryByteBuf buf, ReturnMessagePayload value) {
            buf.writeString(value.message());
            buf.writeInt(value.type().ordinal());
        }
    });
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
