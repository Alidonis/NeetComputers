package com.redtoast.neet.Networking;

import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.graphics.SectoredGraphics;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ColorDisplayGraphicsPayload(BlockPos blockPos, Object graphics)  implements CustomPayload {
    public static final Id<ColorDisplayGraphicsPayload> ID = new Id<>(Identifier.of("neetcomputers", "colordisplaygraphicspayload"));
    public static final PacketCodec<RegistryByteBuf, ColorDisplayGraphicsPayload> CODEC = PacketCodec.of((value, buf) -> {
        //System.out.println("Send: " + RGBGraphicsArray.decimalToRgb(((RGBGraphicsArray) value.graphics).get(0,0)));
        buf.writeBlockPos(value.blockPos());
        SectoredGraphics.encodeFromGraphicsArray(buf, (RGBGraphicsArray) value.graphics);
    }, new PacketCodec<>() {
        @Override
        public ColorDisplayGraphicsPayload decode(RegistryByteBuf buf) {
            BlockPos buffer2 = buf.readBlockPos();
            SectoredGraphics buffer = SectoredGraphics.decodeFromPacket(buf);
            //System.out.println("Receive: " + RGBGraphicsArray.decimalToRgb(buffer.sectors()[0].color()));
            return new ColorDisplayGraphicsPayload(buffer2, buffer);
        }

        @Override
        public void encode(RegistryByteBuf buf, ColorDisplayGraphicsPayload value) {
            //System.out.println("Send: " + RGBGraphicsArray.decimalToRgb(((RGBGraphicsArray) value.graphics).get(0,0)));
            buf.writeBlockPos(value.blockPos());
            SectoredGraphics.encodeFromGraphicsArray(buf, (RGBGraphicsArray) value.graphics);
        }
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}