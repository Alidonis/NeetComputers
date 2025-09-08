package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.graphics.RGBAGraphicsArray;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.value.ValueTypes.Table;

public class ScreenAPI extends GraphicalAPI implements API {
    Runtime runtime;

    public ScreenAPI(RGBGraphicsArray graphics, Computer computer) {
        super(graphics, computer.getRuntime());
        runtime = computer.getRuntime();
    }

    @Override
    public String getLabel() {
        return "screen";
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

    @Exposed
    public Table createLayer(int sizex, int sizey, boolean transparent){
        Layer layer = new Layer(new RGBAGraphicsArray(sizex, sizey, transparent), runtime);
        return APILoader.TableizeAPI(layer, runtime);
    }
}
