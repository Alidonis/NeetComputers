package com.redtoast.graphics;

import net.minecraft.network.PacketByteBuf;
import org.joml.Vector2i;

public class BinaryGraphicsArray {
    private boolean[][] pixels;
    private int sizex, sizey;

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
        for (int i = 0; i < 8; i++){
            buffer = buffer << 1;
            if (array[i]){
                buffer += 1;
            }
        }
        return buffer;
    }

    private static boolean[] ByteToBoolArray(int _byte, int size){
        int buffer = _byte;
        boolean[] array = new boolean[size];
        for (int i = 0; i < size; i++){
            array[i] = buffer%2==1;
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
        for (int i=0; i < y; i++) {
            array[y] = ByteToBoolArray(buf.readShort(),x);
        }
        return new BinaryGraphicsArray(array);
    }
}
