package com.redtoast.graphics;

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
    public BinaryGraphicsArray(boolean[][] pixel){
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
        pixels[sizey-y-1][x] = state;
    }

    public Vector2i getSize(){
        return new Vector2i(sizex,sizey);
    }

    private static void boolArrayToByte(boolean[] array, PacketByteBuf buf){
        int buffer = 0;
        int bitsPacked = 0;
        for (boolean bit : array) {
            if (bitsPacked==15){
                bitsPacked = 0;
                buf.writeShort(buffer);
                buffer = 0;
            }
            if (bit) buffer |= 1<<bitsPacked;
            bitsPacked++;
        }
        buf.writeShort(buffer);
    }

    private static boolean[] ByteToBoolArray(PacketByteBuf buf, int size){
        boolean[] bits = new boolean[size];
        int buffer = buf.readShort();
        int bitsUnpacked = 0;
        for (int i = 0; i < size; i++){
            if (bitsUnpacked==15){
                bitsUnpacked = 0;
                buffer = buf.readShort();
            }
            bits[i] = (buffer>>bitsUnpacked & 1) == 1;
            bitsUnpacked++;
        }
        return bits;
    }

    public void writeScreenToPacketBuf(PacketByteBuf buf) {
        Vector2i size = this.getSize();
        int y = size.y();
        buf.writeShort(y);
        buf.writeShort(size.x());
        for (int i=0; i < y; i++) {
            boolArrayToByte(pixels[i], buf);
        }
    }

    public static BinaryGraphicsArray fromPacket(PacketByteBuf buf) {
        int y = buf.readShort();
        int x = buf.readShort();
        boolean[][] array = new boolean[y][x];
        for (int i = 0; i < y; i++) {
            array[i] = ByteToBoolArray(buf,x);
        }
        return new BinaryGraphicsArray(array);
    }
}
