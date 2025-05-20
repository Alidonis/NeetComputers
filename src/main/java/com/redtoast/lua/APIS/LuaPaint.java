package com.redtoast.lua.APIS;

import com.redtoast.Computer;
import com.redtoast.graphics.GraphicsInterface;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.lua.LuaAPI;
import org.luaj.vm2.LuaValue;

public class LuaPaint extends LuaAPI {
    private Computer computer;
    private RGBGraphicsArray graphicsArray;
    private GraphicsInterface graphics;
    public LuaPaint(Computer comp) {
        super("paint");
        computer = comp;
        graphicsArray = comp.getGraphics();
        graphics = new GraphicsInterface(graphicsArray);

        set("setPixel", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                int x = args[0].toint();
                int y = args[1].toint();
                int oldColor = graphicsArray.get(x,y);
                graphicsArray.set(x,y,args[2].toint());
                return LuaValue.valueOf(oldColor);
            }

            @Override
            public Rules getRules() {
                return new Rules("number").add("number").add("number");
            }
        });

        set("getHexColor", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                return LuaValue.valueOf(RGBGraphicsArray.rgbToDecimal(args[0].toint(),args[1].toint(),args[2].toint()));
            }

            @Override
            public Rules getRules() {
                return new Rules("number").add("number").add("number");
            }
        });
    }
}
