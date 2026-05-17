package com.redtoast.APIS;

import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.annotations.Exposed;

public class DrawableGraphicalAPI extends GraphicalAPI {
    public DrawableGraphicalAPI(RGBGraphicsArray graphics, Runtime runtime) {
        super(graphics, runtime);
    }

    @Exposed
    public void draw()//refreash graphics on screen
    {
        for (int x = 0; x < GraphicsBuffer.getSize().x; x++){
            for (int y = 0; y < GraphicsBuffer.getSize().y; y++){
                Graphics.set(x, y, GraphicsBuffer.get(x, y));
            }
        }
    }
}
