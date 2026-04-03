package com.redtoast.neet.Networking;

import com.redtoast.Connections.PeripheralProvider;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record PeripheralToolScreenInitPayload(String[] listOfFunctions, String modelName, String tag, UUID uuid) implements CustomPayload {
    public static final Id<PeripheralToolScreenInitPayload> ID = new Id<>(Identifier.of("neetcomputers", "peripheraltoolinitpayload"));
    public static final PacketCodec<RegistryByteBuf, PeripheralToolScreenInitPayload> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeShort(value.listOfFunctions().length);
        for (String function : value.listOfFunctions()) buf.writeString(function);
        buf.writeString(value.modelName());
        buf.writeString(value.tag()==null ? "" : value.tag());
        buf.writeUuid(value.uuid());
    }, new PacketCodec<>() {
        @Override
        public PeripheralToolScreenInitPayload decode(RegistryByteBuf buf) {
            String[] listof = new String[buf.readShort()];
            for (int i = 0; i < listof.length; i++) listof[i] = buf.readString();
            String modelName = buf.readString();
            String tag = buf.readString();
            if (tag.isBlank()) tag = null;
            return new PeripheralToolScreenInitPayload(listof, modelName, tag, buf.readUuid());
        }

        @Override
        public void encode(RegistryByteBuf buf, PeripheralToolScreenInitPayload value) {
            buf.writeShort(value.listOfFunctions().length);
            for (String function : value.listOfFunctions()) buf.writeString(function);
            buf.writeString(value.modelName());
            buf.writeString(value.tag()==null ? "" : value.tag());
            buf.writeUuid(value.uuid());
        }
    });
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static PeripheralToolScreenInitPayload fromPeripheral(PeripheralProvider provider){
        return new PeripheralToolScreenInitPayload(provider.getFunctionNames(), provider.getTypeName(), provider.getTag(), provider.getUuid());
    }
}
