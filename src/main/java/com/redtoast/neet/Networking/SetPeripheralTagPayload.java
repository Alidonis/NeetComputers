package com.redtoast.neet.Networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetPeripheralTagPayload(String tag, int syncId) implements CustomPayload {
    public static final Id<SetPeripheralTagPayload> ID = new Id<>(Identifier.of("neetcomputers", "peripheraltoolinitpayload"));
    public static final PacketCodec<RegistryByteBuf, SetPeripheralTagPayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeString(value.tag()==null ? "" : value.tag());
        buf.writeInt(value.syncId());
    }, new PacketCodec<>() {
        @Override
        public SetPeripheralTagPayload decode(RegistryByteBuf buf) {
            return new SetPeripheralTagPayload(buf.readString(), buf.readInt());
        }

        @Override
        public void encode(RegistryByteBuf buf, SetPeripheralTagPayload value) {
            buf.writeString(value.tag()==null ? "" : value.tag());
            buf.writeInt(value.syncId());
        }
    });
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
