package com.redtoast.simulation;

import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.VarType;
import net.minecraft.network.PacketByteBuf;

public class EventGeneric {
    private String Name;
    private List args;
    public EventGeneric(String name, List values){
        Name = name;
        args = values;
    }

    public String getName(){return Name;}
    public List getValues(){return args;}

    //writes the event into a packet, voids complex values
    public PacketByteBuf writeToPacket(PacketByteBuf packet){
        packet.writeString(Name);

        short argCount = 0;
        int typeKeys = 0;//stores the type of each arg, assigns 3 bits to set as a type

        //count args and write types
        for (int i = 0; i < args.size(); i++){
            Value val = args.get(i);
            if (val.instanceOf(VarType.NUMBER)){
                typeKeys <<= 3;
                if ((int)Math.floor(val.toDouble())==val.toInt()){
                    if (!(val.toInt() >= Short.MIN_VALUE && val.toInt() <= Short.MAX_VALUE)){
                        typeKeys++;
                    }
                }else{
                    typeKeys += 2;
                }
                argCount++;
            }else if (val.instanceOf(VarType.BOOLEAN)){
                typeKeys <<= 3;
                typeKeys += 3;
                argCount++;
            }else if (val.instanceOf(VarType.STRING)){
                typeKeys <<= 3;
                typeKeys += 4;
                argCount++;
            }else if (val.isNull()){
                typeKeys <<= 3;
                typeKeys += 5;
                argCount++;
            }
        }

        //write in data
        packet.writeShort(argCount);
        packet.writeInt(typeKeys);

        //write the actual values into the packet, nil is not written and is simply assumed by the reader
        for (int i = 0; i < args.size(); i++){
            Value val = args.get(i);
            if (val.instanceOf(VarType.NUMBER)){
                typeKeys <<= 3;
                if ((int)Math.floor(val.toDouble())==val.toInt()){
                    if (val.toInt() >= Short.MIN_VALUE && val.toInt() <= Short.MAX_VALUE){
                        packet.writeShort((short) (int) val.toInt());
                    }else{
                        packet.writeInt(val.toInt());
                    }
                }else{
                    packet.writeDouble(val.toDouble());
                }
            }else if (val.instanceOf(VarType.BOOLEAN)){
                packet.writeBoolean(val.toBool());
            }else if (val.instanceOf(VarType.STRING)){
                packet.writeString(val.toString());
            }
        }
        return packet;
    }

    public static EventGeneric fromPacket(PacketByteBuf packet){
        String name = packet.readString();
        short argCount = packet.readShort();
        int types = packet.readInt();
        List values = new List();

        //retrieve from packet
        for (int i = 0; i < argCount; i++){
            short type = (short) (types%8);
            types >>= 3;
            switch (type){
                case (0):
                    values.add(Value.of(packet.readShort()));
                case (1):
                    values.add(Value.of(packet.readInt()));
                case (2):
                    values.add(Value.of(packet.readDouble()));
                case (3):
                    values.add(Value.of(packet.readBoolean()));
                case (4):
                    values.add(Value.of(packet.readString()));
                case (5):
                    values.add(Value.of());
            }
        }

        //finalize the data harvested into a event class
        int i = 0;
        Value[] valuesArray = new Value[values.size()];
        for (Value value : values.toArray()){
            valuesArray[i] = value;
            i++;
        }
        return new EventGeneric(name, new List(valuesArray));
    }
}
