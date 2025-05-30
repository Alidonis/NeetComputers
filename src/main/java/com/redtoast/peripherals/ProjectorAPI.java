package com.redtoast.peripherals;

import com.redtoast.Computer;
import com.redtoast.blocks.LargeEntityComputer;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.simulation.LangAPI.LambdaFunction;
import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.Parameter.ParameterRules;
import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.VarType;
import com.redtoast.simulation.peripheral.peripheralAPI;
import org.luaj.vm2.LuaValue;

public class ProjectorAPI extends peripheralAPI {
    Computer computer;
    BinaryGraphicsArray graphics;
    int sizex, sizey;
    public ProjectorAPI(LargeEntityComputer blockEntity) {
        super("holographic_screen", blockEntity, blockEntity.computer);
        computer = blockEntity.computer;
        int sizex = computer.getBinaryGraphics().getSize().x, sizey = computer.getBinaryGraphics().getSize().y;
        graphics = new BinaryGraphicsArray(sizex, sizey);

        set("drawPixel", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                int x = args.get(0).toInt();
                int y = args.get(1).toInt();
                if (x<1 || y<1 || x>sizex || y>sizey) return Value.asError("values not in allowed range");
                graphics.set(x-1,y-1,true);
                return Value.NULL;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.NUMBER).add(VarType.NUMBER);
            }
        });

        set("getSize", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                return Value.toList(new Value[]{new Value(sizex), new Value(sizey)});
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });

        set("drawLine", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                int x1 = args.get(0).toInt();
                int y1 = args.get(1).toInt();
                int x2 = args.get(2).toInt();
                int y2 = args.get(3).toInt();
                if (x1 < 1 || x2 < 1 || y1 < 1 || y2 < 1 || x1 > sizex || x2 > sizex || y1 > sizey || y2 > sizey)
                    return Value.asError("values not in allowed range");
                //equation from here:
                //https://www3.cs.stonybrook.edu/~cse328/2021-lecture-notes/line-drawing.pdf
                int dy = y2 - y1;
                int dx = x2 - x1;
                for (int i = Math.min(x1, x2); i <= Math.max(x1, x2); i++) {
                    int y = (int) Math.round(y1 + (i - x1) * ((double) dy / dx));
                    graphics.set(i - 1, y - 1, true);
                }
                return Value.NULL;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.NUMBER).add(VarType.NUMBER).add(VarType.NUMBER).add(VarType.NUMBER);
            }
        });

        set("drawRec", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                int x1 = args.get(0).toInt();
                int y1 = args.get(1).toInt();
                int x2 = args.get(2).toInt();
                int y2 = args.get(3).toInt();
                if (x1<1 || x2<1 || y1<1 || y2<1 || x1>sizex || x2>sizex || y1>sizey || y2>sizey) return Value.asError("values not in allowed range");
                for (int x = Math.min(x1,x2); x <= Math.max(x1,x2); x++){
                    for (int y = Math.min(y1,y2); y <= Math.max(y1,y2); y++){
                        graphics.set(x-1,y-1,true);
                    }
                }
                return Value.NULL;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules(VarType.NUMBER).add(VarType.NUMBER).add(VarType.NUMBER).add(VarType.NUMBER);
            }
        });

        set("draw", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                computer.setBinaryGraphics(graphics);
                return Value.NULL;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });

        set("clear", new LambdaFunction() {
            @Override
            public Value main(FunctionInput args) {
                graphics = new BinaryGraphicsArray(sizex,sizey);
                return Value.NULL;
            }

            @Override
            public ParameterRules getRules() {
                return new ParameterRules();
            }
        });
    }
}
