package com.redtoast.graphics;

import java.util.LinkedList;

public abstract class FloadFillArray{
    public enum direction{
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    public record order(int x, int y, direction dir){}
    LinkedList<order> orders = new LinkedList<>();
    private final boolean[][] pixels;
    private final int sizex, sizey;

    public FloadFillArray(int sizeX, int sizeY){
        pixels = new boolean[sizeY][sizeX];
        sizex = sizeX;
        sizey = sizeY;
    }

    public boolean check(int x, int y){
        if (x<0 || y<0){
            return true;
        }
        if (x>sizex-1 || y>sizey-1){
            return true;
        }
        return pixels[y][x];
    }

    public void flag(int x, int y){
        pixels[y][x] = true;
    }

    public abstract void setPixel(int x, int y);
    public abstract boolean isValid(int x, int y);

    public int getDirecX(direction dir){
        if (dir==direction.LEFT) return -1;
        if (dir==direction.RIGHT) return 1;
        return 0;
    }

    public int getDirecY(direction dir){
        if (dir==direction.DOWN) return -1;
        if (dir==direction.UP) return 1;
        return 0;
    }

    public direction flip(direction dir){
        return switch (dir){
            case UP -> direction.DOWN;
            case DOWN -> direction.UP;
            case LEFT -> direction.RIGHT;
            case RIGHT -> direction.LEFT;
        };
    }

    public void move(int x, int y, direction dir){
        flag(x, y);
        if (isValid(x, y)) {
            setPixel(x, y);
            orders.add(new order(x, y, dir));
        }
    }

    public void start(int x, int y){
        setPixel(x, y);
        flag(x, y);
        for (int i = 0; i < 4; i ++){
            direction movement = direction.values()[i];
            int x2 = x + getDirecX(movement);
            int y2 = y + getDirecY(movement);
            if (!check(x2, y2)) move(x2, y2, movement);
        }
        while (!orders.isEmpty()){
            order task = orders.getFirst();
            direction anti = flip(task.dir);
            for (int i = 0; i < 4; i++) {
                direction movement = direction.values()[i];
                if (movement!=anti){
                    int x2 = task.x + getDirecX(movement);
                    int y2 = task.y + getDirecY(movement);
                    if (!check(x2, y2)) move(x2, y2, movement);
                }
            }
            orders.remove();
        }
    }
}
