package com.redtoast.peripherals;

import com.redtoast.Computer;
import com.redtoast.blocks.LargeEntityComputer;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.lua.peripheral.peripheralAPI;
import org.luaj.vm2.LuaValue;

public class ProjectorAPI extends peripheralAPI {
    Computer computer;
    BinaryGraphicsArray graphics;
    int sizex, sizey;
    public ProjectorAPI(LargeEntityComputer blockEntity) {
        super("projector", blockEntity, blockEntity.computer);
        computer = blockEntity.computer;
        graphics = blockEntity.computer.getBinaryGraphics();
        sizex = graphics.getSize().x;
        sizey = graphics.getSize().y;

        set("drawPixel", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                int x = args[0].toint();
                int y = args[1].toint();
                if (x<1 || y<1 || x>sizex || y>sizey) return LuaValue.error("values not in allowed range");
                graphics.set(x,y,true);
                return LuaValue.NIL;
            }

            @Override
            public Rules getRules() {
                return new Rules("integer").add("integer");
            }
        });

        set("drawLine", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                int x1 = args[0].toint();
                int y1 = args[1].toint();
                int x2 = args[2].toint();
                int y2 = args[3].toint();
                if (x1<1 || x2<1 || y1<1 || y2<1 || x1>sizex || x2>sizex || y1>sizey || y2>sizey) return LuaValue.error("values not in allowed range");
                //equation from here:
                //https://www3.cs.stonybrook.edu/~cse328/2021-lecture-notes/line-drawing.pdf
                int dy = y2 - y1;
                int dx = x2 - x1;
                for (int i = Math.min(x1,x2); i < Math.max(x1,x2);i++){
                    int y = (int) Math.round(y1+(i-x1)*((double)dy/dx));
                    graphics.set(i,y,true);
                }
                return LuaValue.NIL;
            }

            @Override
            public Rules getRules() {
                return new Rules("integer").add("integer").add("integer").add("integer");
            }
        });

        set("drawRec", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                int x1 = args[0].toint();
                int y1 = args[1].toint();
                int x2 = args[2].toint();
                int y2 = args[3].toint();
                if (x1<1 || x2<1 || y1<1 || y2<1 || x1>sizex || x2>sizex || y1>sizey || y2>sizey) return LuaValue.error("values not in allowed range");
                //equation from here:
                //https://www3.cs.stonybrook.edu/~cse328/2021-lecture-notes/line-drawing.pdf
                for (int x = Math.min(x1,x2); x < Math.max(x1,x2); x++){
                    for (int y = Math.min(y1,y2); y < Math.max(y1,y2); y++){
                        graphics.set(x,y,true);
                    }
                }
                return LuaValue.NIL;
            }

            @Override
            public Rules getRules() {
                return new Rules("integer").add("integer").add("integer").add("integer");
            }
        });

        set("draw", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                computer.broadcastGraphics();
                return LuaValue.NIL;
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });

        set("clear", new LuaFunction() {
            @Override
            public LuaValue main(LuaValue[] args) {
                computer.setBinaryGraphics(new BinaryGraphicsArray(sizex,sizey));
                return LuaValue.NIL;
            }

            @Override
            public Rules getRules() {
                return new Rules();
            }
        });
    }
}
