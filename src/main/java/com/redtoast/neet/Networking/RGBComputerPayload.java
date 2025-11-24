package com.redtoast.neet.Networking;

import com.redtoast.graphics.RGBGraphicsArray;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RGBComputerPayload(RGBGraphicsArray graphicsArray)  implements CustomPayload {
    public static final CustomPayload.Id<RGBComputerPayload> ID = new CustomPayload.Id<>(Identifier.of("neetcomputers", "rgbcomputerpayload"));
    public static final PacketCodec<RegistryByteBuf, RGBComputerPayload> CODEC = PacketCodec.of((value, buf) -> value.graphicsArray.writeScreenToPacketBuf(buf), new PacketCodec<>() {
        @Override
        public RGBComputerPayload decode(RegistryByteBuf buf) {
            return new RGBComputerPayload(RGBGraphicsArray.fromPacket(buf));
        }

        @Override
        public void encode(RegistryByteBuf buf, RGBComputerPayload value) {
            value.graphicsArray.writeScreenToPacketBuf(buf);
        }
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}