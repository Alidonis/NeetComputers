package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.graphics.GraphicsInterface;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;

public class PaintAPI implements API {
    private Computer computer;
    private RGBGraphicsArray graphicsArray;
    private GraphicsInterface graphics;
    public PaintAPI(Computer comp) {
        computer = comp;
        graphicsArray = comp.getGraphics();
        graphics = new GraphicsInterface(graphicsArray);
    }

    @Exposed
    public void setPixel(int x, int y, int color){
        graphicsArray.set(x,y,color);
    }

    @Exposed
    public int getHexColor(int r, int g, int b){
        return RGBGraphicsArray.rgbToDecimal(r,g,b);
    }

    @Override
    public String getLabel() {
        return "paint";
    }
}
