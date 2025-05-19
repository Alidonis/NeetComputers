package com.redtoast.graphics;

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

    public int rgbToDecimal(int red, int green, int blue) {
        //using formula from https://stackoverflow.com/a/18037185
        return (red << 16) & 0xFF0000 | (green << 8) & 0x00FF00 | blue & 0x0000FF;
    }

    public Vector2i getSize(){
        return new Vector2i(sizex,sizey);
    }
}
