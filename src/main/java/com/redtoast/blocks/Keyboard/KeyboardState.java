package com.redtoast.blocks.Keyboard;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

public enum KeyboardState implements StringIdentifiable {
    NORTH,
    SOUTH,
    EAST,
    WEST,
    NORTHWALL,
    SOUTHWALL,
    EASTWALL,
    WESTWALL,
    NORTHDOWN,
    SOUTHDOWN,
    EASTDOWN,
    WESTDOWN,
    NORTHWALLDOWN,
    SOUTHWALLDOWN,
    EASTWALLDOWN,
    WESTWALLDOWN;

    public KeyboardState getDownVariant(){
        return switch (this) {
            case NORTH,NORTHDOWN -> NORTHDOWN;
            case SOUTH,SOUTHDOWN -> SOUTHDOWN;
            case EAST,EASTDOWN -> EASTDOWN;
            case WEST,WESTDOWN -> WESTDOWN;
            case NORTHWALL,NORTHWALLDOWN -> NORTHWALLDOWN;
            case SOUTHWALL,SOUTHWALLDOWN -> SOUTHWALLDOWN;
            case EASTWALL,EASTWALLDOWN -> EASTWALLDOWN;
            case WESTWALL,WESTWALLDOWN -> WESTWALLDOWN;
        };
    }

    public KeyboardState getWallVariant(){
        return switch (this) {
            case NORTH -> NORTHWALL;
            case SOUTH -> SOUTHWALL;
            case EAST -> EASTWALL;
            case WEST -> WESTWALL;
            case NORTHDOWN -> NORTHWALLDOWN;
            case SOUTHDOWN -> SOUTHWALLDOWN;
            case EASTDOWN -> EASTWALLDOWN;
            case WESTDOWN -> WESTWALLDOWN;
            default -> this;
        };
    }

    public static KeyboardState fromDirection(Direction direction){
        return switch (direction){
            case DOWN, UP -> null;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    public Direction getHorizontalDirection(){
        return switch (this){
            case NORTH, NORTHWALL, NORTHDOWN, NORTHWALLDOWN -> Direction.NORTH;
            case SOUTH, SOUTHWALL, SOUTHDOWN, SOUTHWALLDOWN -> Direction.SOUTH;
            case EAST, EASTWALL, EASTDOWN, EASTWALLDOWN -> Direction.EAST;
            case WEST, WESTWALL, WESTDOWN, WESTWALLDOWN -> Direction.WEST;
        };
    }

    public Direction getSupportDirection(){
        return switch (this){
            case NORTH, SOUTH, EAST, WEST, NORTHDOWN, SOUTHDOWN, EASTDOWN, WESTDOWN -> Direction.DOWN;
            case NORTHWALL, NORTHWALLDOWN -> Direction.NORTH;
            case SOUTHWALL, SOUTHWALLDOWN -> Direction.SOUTH;
            case EASTWALL, EASTWALLDOWN -> Direction.EAST;
            case WESTWALL, WESTWALLDOWN -> Direction.WEST;
        };
    }

    public boolean isDown(){
        return ordinal()>=NORTHDOWN.ordinal();
    }

    public boolean isWall(){
        return switch (this) {
            case NORTH, SOUTH, EAST, WEST, NORTHDOWN, SOUTHDOWN, EASTDOWN, WESTDOWN -> false;
            default -> true;
        };
    }

    @Override
    public String asString() {
        return this.toString().toLowerCase();
    }
}
