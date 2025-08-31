package com.redtoast.graphics;

public class RGBAGraphicsArray extends RGBGraphicsArray{
    private boolean transparent = false;
    public boolean[][] memory = null;

    public RGBAGraphicsArray(int sizex, int sizey, boolean transparent) {
        super(sizex, sizey);
        this.transparent = transparent;
        if (transparent){
            memory = new boolean[sizey][sizex];
        }
    }

    public boolean isCellBlank(int x, int y){
        return transparent ? !memory[y][x] : false;
    }

    @Override
    public int get(int x, int y){
        return super.get(x, y) & (isCellBlank(x, y) ? 0x00000011 : 0);
    }

    @Override
    public void set(int x, int y, int color){
        super.set(x, y, color);
        memory[y][x] = true;
    }
}
