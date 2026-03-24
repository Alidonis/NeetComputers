package com.redtoast.simulation.events;

import com.redtoast.graphics.screens.RGBScreenHandler;
import com.redtoast.neet.Networking.EventUploadPayload;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueConvertible;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.VarType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.util.LinkedList;

public class EventGeneric implements ValueConvertible<List> {
    private final String Name;
    private final List args;
    public EventGeneric(String name, List values){
        Name = name;
        args = values;
    }

    public EventGeneric(String name, Value... values){
        Name = name;
        args = new List(values);
    }

    public String getName(){return Name;}
    public List getValues(){return args;}
    public Value<List> asValue(){
        List list = args.duplicate();
        list.addFirst(Value.of(Name));
        return list.asValue();
    }

    //writes the event into a packet, voids complex values
    public PacketByteBuf writeToPacket(PacketByteBuf packet){
        packet.writeString(Name);

        packet.writeShort(args.size());
        for (Value<?> value : args){
            switch (value.getType()){
                case INT -> {
                    packet.writeShort(0);
                    packet.writeInt(value.toInt());
                }
                case DOUBLE -> {
                    packet.writeShort(1);
                    packet.writeDouble(value.toDouble());
                }
                case FLOAT -> {
                    packet.writeShort(2);
                    packet.writeFloat(value.toFloat());
                }
                case STRING -> {
                    packet.writeShort(3);
                    packet.writeString(value.toString());
                }
                case BOOLEAN -> {
                    packet.writeShort(4);
                    packet.writeBoolean(Boolean.TRUE.equals(value.toBool()));
                }
                default -> packet.writeShort(5);
            }
        }
        return packet;
    }

    private static VarType[] codex = new VarType[]{VarType.INT, VarType.DOUBLE, VarType.FLOAT, VarType.STRING, VarType.BOOLEAN, VarType.NULL};
    public static EventGeneric fromPacket(PacketByteBuf packet){
        String name = packet.readString();
        int size = packet.readShort();
        List values = new List();

        for (int i = 0; i < size; i++){
            VarType type = codex[packet.readShort()];
            switch (type){
                case INT -> values.add(Value.of(packet.readInt()));
                case DOUBLE -> values.add(Value.of(packet.readDouble()));
                case FLOAT -> values.add(Value.of(packet.readFloat()));
                case STRING -> values.add(Value.of(packet.readString()));
                case BOOLEAN -> values.add(Value.of(packet.readBoolean()));
                case NULL -> values.add(Value.NULL);
                default -> Function.logError("event packet received with invalid type " + type);
            }
        }

        return new EventGeneric(name, values);
    }

    public void send(RGBScreenHandler handler){
        ClientPlayNetworking.send(new EventUploadPayload(this, handler.syncId));
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof EventGeneric event){
            return event.getName().equals(Name);
        }
        return super.equals(obj);
    }

    @Override
    public String toString(){
        return "event<"+getName()+"> "+args.toString();
    }

    @FunctionalInterface
    public interface eventCallback{
        void onEvent(EventGeneric eventGeneric);
    }
}
