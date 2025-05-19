package com.redtoast.lua.APIS;

import com.redtoast.Computer;
import com.redtoast.graphics.GraphicsInterface;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.lua.LuaAPI;

public class LuaPaint extends LuaAPI {
    private Computer computer;
    private RGBGraphicsArray graphicsArray;
    private GraphicsInterface graphics;
    public LuaPaint(Computer comp) {
        super("paint");
        computer = comp;
        graphicsArray = comp.getGraphics();
        graphics = new GraphicsInterface(graphicsArray);


    }
}
