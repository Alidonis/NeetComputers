package com.redtoast.simulation.APIS;

import com.redtoast.Computer;
import com.redtoast.graphics.GraphicsInterface;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.LangAPI.LangAPI;
import com.redtoast.simulation.LangAPI.LambdaFunction;
import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.Parameter.ParameterRules;
import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.VarType;

public class PaintAPI extends LangAPI {
    private Computer computer;
    private RGBGraphicsArray graphicsArray;
    private GraphicsInterface graphics;
    public PaintAPI(Computer comp) {
        super("paint");
        computer = comp;
        graphicsArray = comp.getGraphics();
        graphics = new GraphicsInterface(graphicsArray);

        set("setPixel", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                int x = args.get(0).toInt();
                int y = args.get(1).toInt();
                int oldColor = graphicsArray.get(x,y);
                graphicsArray.set(x,y,args.get(2).toInt());
                return new Value(oldColor);
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.NUMBER).add(VarType.NUMBER).add(VarType.NUMBER);
            }
        });

        set("getHexColor", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                return new Value(RGBGraphicsArray.rgbToDecimal(args.get(0).toInt(),args.get(1).toInt(),args.get(2).toInt()));
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.NUMBER).add(VarType.NUMBER).add(VarType.NUMBER);
            }
        });
    }
}
