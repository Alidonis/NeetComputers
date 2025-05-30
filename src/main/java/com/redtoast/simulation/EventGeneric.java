package com.redtoast.simulation;

import net.minecraft.network.PacketByteBuf;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.LinkedList;

public class EventGeneric {
    private String Name;
    private Varargs args;
    public EventGeneric(String name, Varargs values){
        Name = name;
        args = values;
    }

    public String getName(){return Name;}
    public Varargs getValues(){return args;}

    //writes the event into a packet, voids complex values
    public PacketByteBuf writeToPacket(PacketByteBuf packet){
        packet.writeString(Name);

        short argCount = 0;
        int typeKeys = 0;//stores the type of each arg, assigns 3 bits to set as a type

        //count args and write types
        for (int i = 0; i < args.narg(); i++){
            LuaValue val = args.arg(i-1);
            String type = val.typename();
            if (type.equals("number")){
                typeKeys <<= 3;
                if ((int)Math.floor(val.todouble())==val.toint()){
                    if (!(val.toint() >= Short.MIN_VALUE && val.toint() <= Short.MAX_VALUE)){
                        typeKeys++;
                    }
                }else{
                    typeKeys += 2;
                }
                argCount++;
            }else if (type.equals("boolean")){
                typeKeys <<= 3;
                typeKeys += 3;
                argCount++;
            }else if (type.equals("string")){
                typeKeys <<= 3;
                typeKeys += 4;
                argCount++;
            }else if (type.equals("nil")){
                typeKeys <<= 3;
                typeKeys += 5;
                argCount++;
            }
        }

        //write in data
        packet.writeShort(argCount);
        packet.writeInt(typeKeys);

        //write the actual values into the packet, nil is not written and is simply assumed by the reader
        for (int i = 0; i < args.narg(); i++){
            LuaValue val = args.arg(i-1);
            String type = val.typename();
            if (type.equals("number")){
                typeKeys <<= 3;
                if ((int)Math.floor(val.todouble())==val.toint()){
                    if (val.toint() >= Short.MIN_VALUE && val.toint() <= Short.MAX_VALUE){
                        packet.writeShort(val.toshort());
                    }else{
                        packet.writeInt(val.toint());
                    }
                }else{
                    packet.writeDouble(val.todouble());
                }
            }else if (type.equals("boolean")){
                packet.writeBoolean(val.toboolean());
            }else if (type.equals("string")){
                packet.writeString(val.toString());
            }
        }
        return packet;
    }

    public static EventGeneric fromPacket(PacketByteBuf packet){
        String name = packet.readString();
        short argCount = packet.readShort();
        int types = packet.readInt();
        LinkedList<LuaValue> values = new LinkedList<>();

        //retrieve from packet
        for (int i = 0; i < argCount; i++){
            short type = (short) (types%8);
            types >>= 3;
            switch (type){
                case (0):
                    values.add(LuaValue.valueOf(packet.readShort()));
                case (1):
                    values.add(LuaValue.valueOf(packet.readInt()));
                case (2):
                    values.add(LuaValue.valueOf(packet.readDouble()));
                case (3):
                    values.add(LuaValue.valueOf(packet.readBoolean()));
                case (4):
                    values.add(LuaValue.valueOf(packet.readString()));
                case (5):
                    values.add(LuaValue.NIL);
            }
        }

        //finalize the data harvested into a event class
        int i = 0;
        LuaValue[] valuesArray = new LuaValue[values.size()];
        for (LuaValue value : values){
            valuesArray[i] = value;
            i++;
        }
        return new EventGeneric(name, LuaValue.varargsOf(valuesArray));
    }
}
