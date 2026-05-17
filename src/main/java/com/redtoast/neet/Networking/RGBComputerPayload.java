package com.redtoast.neet.Networking;

import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.graphics.SectoredGraphics;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RGBComputerPayload(Object graphics)  implements CustomPayload {
    public static final CustomPayload.Id<RGBComputerPayload> ID = new CustomPayload.Id<>(Identifier.of("neetcomputers", "rgbcomputerpayload"));
    public static final PacketCodec<RegistryByteBuf, RGBComputerPayload> CODEC = PacketCodec.of((value, buf) -> SectoredGraphics.encodeFromGraphicsArray(buf, (RGBGraphicsArray) value.graphics), new PacketCodec<>() {
        @Override
        public RGBComputerPayload decode(RegistryByteBuf buf) {
            return new RGBComputerPayload(SectoredGraphics.decodeFromPacket(buf));
        }

        @Override
        public void encode(RegistryByteBuf buf, RGBComputerPayload value) {
            SectoredGraphics.encodeFromGraphicsArray(buf, (RGBGraphicsArray) value.graphics);
        }
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}