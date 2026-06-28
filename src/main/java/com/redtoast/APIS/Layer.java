package com.redtoast.APIS;

import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import com.redtoast.simulation.value.VarType;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public class Layer extends GraphicalAPI{
    public Layer(RGBGraphicsArray graphics, Runtime runtime) {
        super(graphics, runtime);
        GraphicsBuffer.makeTransparent();
        Graphics = null;
    }

    @Override
    public Table postProcessing(Table self){
        self.put("_data", getAsArray().asValue());
        return self;
    }

    /**
     * Reads the layer and returns its data as an array of packed colors
     * @return the array of packed colors
     */
    @Exposed
    public List getAsArray(){
        List list = new List();
        for (int[] numb : GraphicsBuffer.pixels){
            List subList = new List();
            for (int num : numb) {
                subList.add(Value.of(num));
            }
            list.add(subList.asValue());
        }
        return list;
    }

    public static int[][] translateArray(List list) {
        if (list == null) throw new ExposedError("Invalid layer (no data)");
        if (list.isEmpty()) throw new ExposedError("Invalid layer (list lengths cant be zero)");
        if (!list.get(0).instanceOf(VarType.LIST)) throw new ExposedError("Invalid layer (failed to produce list)");
        int width = list.get(0).toList().size();
        if (width==0) throw new ExposedError("Invalid layer (list lengths cant be zero)");
        int[][] array = new int[list.size()][width];
        int y = 0;
        for (Value<?> subvalue : list) {
            if (!subvalue.instanceOf(VarType.LIST)) throw new ExposedError("Invalid layer (failed to produce list)");
            List sublist = subvalue.toList();
            if (sublist.size()!=width) throw new ExposedError("Invalid layer (inconsistent list sizes)");
            for (int x = 0; x < width; x++) {
                Value pixel = sublist.get(x);
                if (pixel.getValue() instanceof Integer color){
                    array[y][x] = color;
                }else{
                    throw new ExposedError("Invalid layer (array contained invalid pixel)");
                }
            }
            y++;
        }
        return array;
    }
}
