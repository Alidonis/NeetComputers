package com.redtoast.neet.Networking;

import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.RGBGraphicsArray;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record BinaryGraphicsPayload(BlockPos blockPos, BinaryGraphicsArray graphicsArray)  implements CustomPayload {
    public static final Id<BinaryGraphicsPayload> ID = new Id<>(Identifier.of("neetcomputers", "binarygraphicspayload"));
    public static final PacketCodec<RegistryByteBuf, BinaryGraphicsPayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBlockPos(value.blockPos());
        value.graphicsArray.writeScreenToPacketBuf(buf);
    }, new PacketCodec<>() {
        @Override
        public BinaryGraphicsPayload decode(RegistryByteBuf buf) {
            return new BinaryGraphicsPayload(buf.readBlockPos(), BinaryGraphicsArray.fromPacket(buf));
        }

        @Override
        public void encode(RegistryByteBuf buf, BinaryGraphicsPayload value) {
            buf.writeBlockPos(value.blockPos());
            value.graphicsArray.writeScreenToPacketBuf(buf);
        }
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}