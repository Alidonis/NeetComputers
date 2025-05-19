package com.redtoast.graphics;

import org.joml.Vector2i;

public class BianaryGraphicsArray {
    private boolean[][] array;
    private int sizex, sizey;

    public BianaryGraphicsArray(int sizeX, int sizeY){
        array = new boolean[sizeX][sizeY];
        sizex = sizeX;
        sizey = sizeY;
    }

    public boolean get(int x, int y){
        return array[x][y];
    }

    public void set(int x, int y, boolean state){
        array[x][y] = state;
    }

    public Vector2i getSize(){
        return new Vector2i(sizex,sizey);
    }
}
