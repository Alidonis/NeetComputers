package com.redtoast.neet.Networking;

import com.redtoast.Connections.PipeType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record PipeBufferPayload(PipeType type, BlockPos[] buffer)  implements CustomPayload {
    public static final Id<PipeBufferPayload> ID = new Id<>(Identifier.of("neetcomputers", "pipebufferpayload"));
    public static final PacketCodec<RegistryByteBuf, PipeBufferPayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeShort(value.type.ordinal());
        buf.writeInt(value.buffer.length);
        for (BlockPos pos : value.buffer){
            buf.writeBlockPos(pos);
        }
    }, new PacketCodec<>() {
        @Override
        public void encode(RegistryByteBuf buf, PipeBufferPayload value) {
            buf.writeShort(value.type.ordinal());
            buf.writeInt(value.buffer.length);
            for (BlockPos pos : value.buffer){
                buf.writeBlockPos(pos);
            }
        }

        @Override
        public PipeBufferPayload decode(RegistryByteBuf buf) {
            PipeType type = PipeType.values()[buf.readShort()];
            int length = buf.readInt();
            BlockPos[] buffer = new BlockPos[length];
            for (int i = 0; i < length; i++){
                buffer[i] = buf.readBlockPos();
            }
            return new PipeBufferPayload(type, buffer);
        }
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}