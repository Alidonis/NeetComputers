package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.ValueTypes.Tuple;

public class ProjectorAPI implements API {
    Computer computer;
    BinaryGraphicsArray graphics;
    int sizex, sizey;
    public ProjectorAPI(Computer computer) {
        this.computer = computer;
        sizex = this.computer.getBinaryGraphics().getSize().x;
        sizey = this.computer.getBinaryGraphics().getSize().y;
        graphics = new BinaryGraphicsArray(sizex, sizey);
    }

    @Exposed
    public void drawPixel(int x, int y){
        if (x<1 || y<1 || x>sizex || y>sizey) {
            throw new ExposedError("values not in allowed range2");
        }
        graphics.set(x-1,y-1,true);
    }

    @Exposed
    public Tuple getSize(){
        return new Tuple(sizex, sizey);
    }

    @Exposed
    public void drawLine(int x1, int y1, int x2, int y2){
        if (x1<1 || x2<1 || y1<1 || y2<1 || x1>sizex || x2>sizex || y1>sizey || y2>sizey) {
            throw new ExposedError("values not in allowed range2");
        }
        for (int x = Math.min(x1,x2); x <= Math.max(x1,x2); x++){
            for (int y = Math.min(y1,y2); y <= Math.max(y1,y2); y++){
                graphics.set(x-1,y-1,true);
            }
        }
    }

    @Exposed
    public void drawRec(int x1, int y1, int x2, int y2){
        if (x1<1 || x2<1 || y1<1 || y2<1 || x1>sizex || x2>sizex || y1>sizey || y2>sizey) {
            throw new ExposedError("values not in allowed range2");
        }
        for (int x = Math.min(x1,x2); x <= Math.max(x1,x2); x++){
            for (int y = Math.min(y1,y2); y <= Math.max(y1,y2); y++){
                graphics.set(x-1,y-1,true);
            }
        }
    }

    @Exposed
    public void draw(){
        computer.setBinaryGraphics(graphics);
    }

    @Exposed
    public void clear(){
        graphics = new BinaryGraphicsArray(sizex,sizey);
    }

    @Override
    public String getLabel() {
        return "projector screen";
    }
}
