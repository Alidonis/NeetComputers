package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.simulation.base.API;

public class GraphicsAPI implements API {
    public GraphicsAPI(Computer comp){}
    @Override
    public String getLabel() {
        return "graphics";
    }
}