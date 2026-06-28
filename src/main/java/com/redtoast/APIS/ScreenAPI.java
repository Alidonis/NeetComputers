package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.graphics.RGBAGraphicsArray;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.ValueTypes.Table;

public class ScreenAPI extends DrawableGraphicalAPI implements API {
    Computer computer;

    public ScreenAPI(RGBGraphicsArray graphics, Computer computer) {
        super(graphics, computer.getRuntime());
        this.computer = computer;
    }

    @Override
    public String getLabel() {
        return "screen";
    }

    @Exposed
    @Override
    public void draw(){
        super.draw();
        computer.renderColorGraphics();
    }

    @Exposed
    public Table createLayer(int sizex, int sizey){
        if (sizex==0 || sizey==0) throw new ExposedError("Size cant be zero");
        Layer layer = new Layer(new RGBGraphicsArray(sizex, sizey), computer.getRuntime());
        return APILoader.TableizeAPI(layer, computer.getRuntime());
    }
}
