package com.redtoast.neet.Networking;

import com.redtoast.simulation.events.EventGeneric;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**Networking payload to transfer events registered on client side computers to their server side equivalent **/
public record EventUploadPayload(EventGeneric event, int syncId) implements CustomPayload {
    public static final Id<EventUploadPayload> ID = new Id<>(Identifier.of("neetcomputers", "eventpploadpayload"));
    public static final PacketCodec<RegistryByteBuf, EventUploadPayload> CODEC = PacketCodec.of((value, buf) -> {
        value.event.writeToPacket(buf);
        buf.writeInt(value.syncId);
    }, new PacketCodec<>() {
        @Override
        public EventUploadPayload decode(RegistryByteBuf buf) {
            return new EventUploadPayload(EventGeneric.fromPacket(buf), buf.readInt());
        }

        @Override
        public void encode(RegistryByteBuf buf, EventUploadPayload value) {
            value.event.writeToPacket(buf);
            buf.writeInt(value.syncId);
        }
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
