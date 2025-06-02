package com.redtoast.simulation.APIS;

import com.redtoast.simulation.LangAPI.*;
import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.ValueTypes.Function;

import java.util.LinkedList;

public class GraphicsAPI implements API {
    public @InsertAtRuntime LinkedList<Function> runtimeFuncs = new LinkedList<>();
    @Override
    public String getLabel() {
        return "graphics";
    }

    public GraphicsAPI(){
        runtimeFuncs.add(new Function("bob") {
            @Override
            public Value call(FunctionInput parameters) {
                return (new Value("fuck me i hate life"));
            }
        });
    }

    static class filter extends CustomParameter{
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
    public int piss(@CustomRule(rule = filter.class) Value hell, boolean cencer){
        if (cencer){
            System.out.println("frick yea");
        }else{
            System.out.println("fuck yea");
        }
        System.out.println(hell.toString());
        return (int) Math.round(Math.random()+1);
    }
}
