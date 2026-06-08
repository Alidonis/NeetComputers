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

public record ComputerDataComponent(int address, boolean isOn, UUID id) {
    private static final ByteBuffer empty = serialize(new NbtCompound());
    public static final Codec<ComputerDataComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("address").forGetter(ComputerDataComponent::address),
            Codec.BOOL.fieldOf("isOn").forGetter(ComputerDataComponent::isOn),
            Codec.STRING.fieldOf("id").forGetter(ComputerDataComponent::getId)
    ).apply(builder, ComputerDataComponent::reconstruct));
    public static ComponentType<ComputerDataComponent> TYPE;

    public static ComputerDataComponent reconstruct(int address, boolean isOn, String idSerial){
        return new ComputerDataComponent(address, isOn, UUID.fromString(idSerial));
    }

    private String getId(){
        return id().toString();
    }

    private static ByteBuffer serialize(NbtCompound nbt){
        LinkedList<Byte> bytes = new LinkedList<>();
        try{
            NbtIo.write(nbt, new DataOutputStream(new OutputStream() {
                @Override
                public void write(int b) {
                    bytes.add((byte)b);
                }
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] buffer = new byte[bytes.size()];
        for (int i = 0; i < buffer.length; i++) buffer[i] = bytes.get(i);
        return ByteBuffer.wrap(buffer);
    }

    private static NbtCompound deserialize(ByteBuffer buffer){
        try{
            return NbtIo.readCompound(new DataInputStream(new InputStream() {
                @Override
                public int read() throws IOException {
                    return (buffer.remaining() != 0 ? buffer.get() : -1) & 0xFF;
                }
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
