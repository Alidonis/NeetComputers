package com.redtoast.neet.Networking;

import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.graphics.SectoredGraphics;
import com.redtoast.graphics.screens.RGBGraphicsScreen;
import com.redtoast.graphics.screens.RGBScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record ComputerScreenInitPayload(Object graphics, UUID uuid)  implements CustomPayload {
    public static final Id<ComputerScreenInitPayload> ID = new Id<>(Identifier.of("neetcomputers", "computerscreeninitpayload"));
    public static final PacketCodec<RegistryByteBuf, ComputerScreenInitPayload> CODEC = PacketCodec.of((value, buf) -> {
        SectoredGraphics.encodeFromGraphicsArray(buf, (RGBGraphicsArray) value.graphics);
        buf.writeUuid(value.uuid());
    }, new PacketCodec<>() {
        @Override
        public ComputerScreenInitPayload decode(RegistryByteBuf buf) {
            return new ComputerScreenInitPayload(SectoredGraphics.decodeFromPacket(buf), buf.readUuid());
        }

        @Override
        public void encode(RegistryByteBuf buf, ComputerScreenInitPayload value) {
            SectoredGraphics.encodeFromGraphicsArray(buf, (RGBGraphicsArray) value.graphics);
            buf.writeUuid(value.uuid());
        }
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}