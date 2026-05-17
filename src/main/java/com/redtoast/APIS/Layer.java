package com.redtoast.APIS;

import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.base.LangThread;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.UUID;

public class Layer extends GraphicalAPI{
    private final UUID uuid;
    protected static final Hashtable<UUID, Layer> memoryTable = new Hashtable<>();

    public Layer(RGBGraphicsArray graphics, Runtime runtime) {
        super(graphics, runtime);
        uuid = UUID.randomUUID();
        memoryTable.put(uuid, this);
        GraphicsBuffer.makeTransparent();
        Graphics = null;
    }

    @Override
    public void onCall(Runtime runtime, Method method){
        try{
            if (!memoryTable.containsKey(uuid) && method != getClass().getMethod("isClosed")){
                throw new ExposedError("Attempted to use closed layer");
            }
        }catch (NoSuchMethodException ignored){}
    }

    @Override
    public Table postProcessing(Table self){
        self.put("_uuid", uuid.toString());
        return self;
    }

    /**
     * tests to see if the layers resources are available
     * @return boolean if closed
     */
    @Exposed
    public boolean isClosed(){return !memoryTable.containsKey(uuid);}

    /**
     * Closes the current layer, freeing its resources
     */
    @Exposed
    public void close(){
        memoryTable.remove(uuid);
        GraphicsBuffer = null;
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
}
