package com.redtoast.simulation.APIS;

import com.redtoast.simulation.LangAPI.*;

public class GraphicsAPI implements API {
    @Override
    public String getLabel() {
        return "graphics";
    }

    static class filter extends CustomParameter{
        public filter(){

        }

        @Override
        public boolean rule(Value arg) {
            if (!arg.instanceOf(VarType.STRING)) return false;
            return arg.toString().startsWith("f");
        }

        @Override
        public String getName() {
            return "f starting word";
        }
    }

    @Exposed
    public int piss(Value hell, boolean cencer){
        if (cencer){
            System.out.println("frick yea");
        }else{
            System.out.println("fuck yea");
        }
        System.out.println(hell.toString());
        return (int) Math.round(Math.random()+1);
    }
}
