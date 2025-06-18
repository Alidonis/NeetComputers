package com.redtoast.graphics;

import net.minecraft.network.PacketByteBuf;
import org.joml.Vector2i;

public class RGBGraphicsArray {
    public int[][] pixels;
    private int sizex, sizey;

    public RGBGraphicsArray(int sizex,int sizey) {
        this.sizex = sizex;
        this.sizey = sizey;
        pixels = new int[sizey][sizex];
    }
    public RGBGraphicsArray(int[][] arr) {
        this.sizex = arr[0].length;
        this.sizey = arr.length;
        pixels = arr;
    }

    public int get(int x, int y) {
        return pixels[y][x];
    }

    public void set(int x, int y, int color){
        pixels[y][x] = color;
    }

    public static int rgbToDecimal(int red, int green, int blue) {
        //using formula from https://stackoverflow.com/a/18037185
        return (red << 16) & 0xFF0000 | (green << 8) & 0x00FF00 | blue & 0x0000FF;
    }

    public void writeScreenToPacketBuf(PacketByteBuf buf) {
        Vector2i size = this.getSize();
        int y = size.y();
        buf.writeInt(y);
        buf.writeInt(size.x());
        for (int i=0; i < y; i++) {
            buf.writeIntArray(this.pixels[i]);
        }
    }

    public static RGBGraphicsArray fromPacket(PacketByteBuf buf) {
        int y = buf.readInt();
        int x = buf.readInt();
        int[][] arr = new int[y][x];
        for (int i=0; i < y; i++) {
            arr[i] = buf.readIntArray(x);
        }
        return new RGBGraphicsArray(arr);
    }

    public Vector2i getSize(){
        return new Vector2i(sizex,sizey);
    }

    public int getAmount(){
        return sizex * sizey;
    }

    public void clear(){
        for (int x = 0; x < getSize().x; x++){
            for (int y = 0; y < getSize().y; y++){
                set(x,y,0);
            }
        }
    }
}
