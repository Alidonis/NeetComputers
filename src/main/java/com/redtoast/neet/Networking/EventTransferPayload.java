package com.redtoast.neet.Networking;

import com.redtoast.simulation.EventGeneric;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**Networking payload to transfer events registered on client side computers to their server side equivalent **/
public record EventTransferPayload(UUID uuid, EventGeneric event) implements CustomPayload {
    public static final CustomPayload.Id<EventTransferPayload> ID = new CustomPayload.Id<>(Identifier.of("neetcomputers", "eventtransferpayload"));
    public static final PacketCodec<RegistryByteBuf, EventTransferPayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeUuid(value.uuid());
        value.event.writeToPacket(buf);
    }, new PacketCodec<>() {
        @Override
        public EventTransferPayload decode(RegistryByteBuf buf) {
            return new EventTransferPayload(buf.readUuid(), EventGeneric.fromPacket(buf));
        }

        @Override
        public void encode(RegistryByteBuf buf, EventTransferPayload value) {
            buf.writeUuid(value.uuid());
            value.event.writeToPacket(buf);
        }
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
