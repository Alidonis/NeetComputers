package com.redtoast.graphics;

import org.joml.Vector2i;

public class BianaryGraphicsArray {
    private boolean[][] array;
    private int sizex, sizey;

    public BianaryGraphicsArray(int sizeX, int sizeY){
        array = new boolean[sizeY][sizeX];
        sizex = sizeX;
        sizey = sizeY;
    }

    public boolean get(int x, int y){
        return array[y][x];
    }

    public void set(int x, int y, boolean state){
        array[y][x] = state;
    }

    public Vector2i getSize(){
        return new Vector2i(sizex,sizey);
    }
}
