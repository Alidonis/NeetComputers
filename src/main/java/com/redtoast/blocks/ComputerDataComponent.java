package com.redtoast.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.UUID;

public record ComputerDataComponent(int address, boolean isOn, UUID id, String template) {
    public static final Codec<ComputerDataComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("address").forGetter(ComputerDataComponent::address),
            Codec.BOOL.fieldOf("isOn").forGetter(ComputerDataComponent::isOn),
            Codec.STRING.fieldOf("id").forGetter(ComputerDataComponent::getId),
            Codec.STRING.fieldOf("template").forGetter(ComputerDataComponent::template)
    ).apply(builder, ComputerDataComponent::reconstruct));
    public static ComponentType<ComputerDataComponent> TYPE;

    public static ComputerDataComponent reconstruct(int address, boolean isOn, String idSerial, String template){
        return new ComputerDataComponent(address, isOn, UUID.fromString(idSerial), template);
    }

    private String getId(){
        return id().toString();
    }
}
