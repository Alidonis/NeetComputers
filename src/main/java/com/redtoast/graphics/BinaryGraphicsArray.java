package com.redtoast.graphics;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.joml.Vector2i;

public class BinaryGraphicsArray {
    private final boolean[][] pixels;
    private final int sizex, sizey;

    public BinaryGraphicsArray(int sizeX, int sizeY){
        pixels = new boolean[sizeY][sizeX];
        sizex = sizeX;
        sizey = sizeY;
    }
    private BinaryGraphicsArray(boolean[][] pixel){
        pixels = pixel;
        sizey = pixel.length;
        sizex = pixel[0].length;
    }

    public boolean get(int x, int y){
        if (x<0 || y<0){
            return true;
        }
        if (x>sizex-1 || y>sizey-1){
            return true;
        }
        return pixels[y][x];
    }

    public void set(int x, int y, boolean state){
        pixels[y][x] = state;
    }

    public Vector2i getSize(){
        return new Vector2i(sizex,sizey);
    }

    public int getAmount(){
        return sizex * sizey;
    }

    private static int boolArrayToByte(boolean[] array){
        int buffer = 0;
        for (int i = 0; i < array.length; i++){
            buffer = buffer << 1;
            if (array[i]){
                buffer++;
            }
        }
        return buffer;
    }

    private static boolean[] ByteToBoolArray(int _byte, int size){
        int buffer = _byte;
        boolean[] array = new boolean[size];
        for (int i = 0; i < size; i++){
            array[size-i-1] = buffer%2==1;
            buffer = buffer >> 1;
        }
        return array;
    }

    public void writeScreenToPacketBuf(PacketByteBuf buf) {
        Vector2i size = this.getSize();
        int y = size.y();
        buf.writeShort(y);
        buf.writeShort(size.x());
        for (int i=0; i < y; i++) {
            buf.writeShort(boolArrayToByte(pixels[i]));
        }
    }

    public static BinaryGraphicsArray fromPacket(PacketByteBuf buf) {
        int y = buf.readShort();
        int x = buf.readShort();
        boolean[][] array = new boolean[y][x];
        for (int i = 0; i < y; i++) {
            array[i] = ByteToBoolArray(buf.readShort(),x);
        }
        return new BinaryGraphicsArray(array);
    }

    public NbtCompound writeScreenToNBT(){
        Vector2i size = this.getSize();
        NbtCompound nbt = new NbtCompound();
        nbt.putShort("sizeX", (short) size.x);
        nbt.putShort("sizeY", (short) size.y);
        for (int i=0; i < size.y; i++) {
            nbt.putShort(""+i, (short) boolArrayToByte(pixels[i]));
        }
        return nbt;
    }

    public static BinaryGraphicsArray fromNbt(NbtCompound nbt){
        int x = nbt.getShort("sizeX");
        int y = nbt.getShort("sizeY");
        boolean[][] array = new boolean[y][x];
        for (int i = 0; i < y; i++) {
            array[i] = ByteToBoolArray(nbt.getShort(""+i),x);
        }
        return new BinaryGraphicsArray(array);
    }
}
