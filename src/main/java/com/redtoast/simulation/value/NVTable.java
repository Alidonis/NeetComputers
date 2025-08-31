package com.redtoast.simulation.value;

import com.redtoast.Computer;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import net.minecraft.nbt.*;

import java.util.HashMap;
import java.util.Objects;

public class NVTable extends HashMap<String, Value> {
    private final Computer computer;
    public NVTable(Computer computer){
        this.computer = computer;
    }

    public boolean isntPrimitive(Value value){
        return !switch (value.getType()) {
            case NULL, TABLE, LIST, INT, DOUBLE, FLOAT, STRING, BOOLEAN:
                yield true;
            default:
                yield false;
        };
    }

    @Override
    public Value put(String key, Value value){
        if (isntPrimitive(value)){
            computer.crash("Attempted to assign non-primitive value to NV ram");
            return value;
        }
        super.put(key, value);
        return value;
    }

    @Override
    public Value remove(Object key){
        computer.saveNBT();
        return super.remove(key);
    }

    public static NbtElement convert(Value value) {
        if (value==null) return null;
        NbtElement element = null;
        switch (value.getType()) {
            case FLOAT -> element = NbtFloat.of(Objects.requireNonNull(value.toFloat()));
            case INT -> element = NbtInt.of(Objects.requireNonNull(value.toInt()));
            case DOUBLE -> element = NbtDouble.of(Objects.requireNonNull(value.toDouble()));
            case STRING -> element = NbtString.of(Objects.requireNonNull(value.toString()));
            case BOOLEAN -> element = NbtByte.of(Objects.requireNonNull(value.toBool()));
        }
        if (element == null){
            if (value.instanceOf(VarType.TABLE)) {
                Table table = value.toTable();
                NbtCompound compound = new NbtCompound();
                table.foreach((key, data) -> {
                    if (key.instanceOf(VarType.STRING)){
                        NbtElement temp = convert(data);
                        if (temp!=null) compound.put(key.toString(), temp);
                    }
                });
                return compound;
            }
            if (value.instanceOf(VarType.LIST)){
                List list = value.toList();
                NbtList nbtList = new NbtList();
                for (Value data : list.toArray()){
                    NbtElement temp = convert(data);
                    if (temp!=null) nbtList.add(temp);
                }
                return nbtList;
            }
            return null;
        }else{
            return element;
        }
    }

    public NbtCompound serialize(){
        NbtCompound compound = new NbtCompound();
        forEach((key, value) -> {
            compound.put(key, convert(value));
        });
        return compound;
    }

    public static Value deconvert(NbtElement element){
        if (element==null) return null;
        Value value;
        if (element instanceof NbtByte val) value = Value.of(val.byteValue() != 0);
        else if (element instanceof NbtInt val) value = Value.of(val.intValue());
        else if (element instanceof NbtDouble val) value = Value.of(val.doubleValue());
        else if (element instanceof NbtFloat val) value = Value.of(val.floatValue());
        else if (element instanceof NbtString val) value = Value.of(val.asString());
        else value = null;
        if (value!=null) return value;
        if (element instanceof NbtCompound compound){
            Table table = new Table();
            for (String string : compound.getKeys()) {
                value = deconvert(Objects.requireNonNull(compound.get(string)));
                if (value!=null) table.put(string, value);
            }
            return table.asValue();
        }
        if (element instanceof NbtList nbtList){
            List list = new List();
            for (NbtElement val : nbtList){
                value = deconvert(val);
                if (value!=null) list.add(value);
            }
            return list.asValue();
        }
        return null;
    }

    public static NVTable deserialize(NbtCompound compound, Computer computer){
        Value value = deconvert(compound);
        assert value != null;
        NVTable table = new NVTable(computer);
        Objects.requireNonNull(value.toTable()).foreach((key, data) -> table.put(key.toString(), data));
        return table;
    }
}
