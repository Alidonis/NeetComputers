package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;

/**
 * represents an N.E.E.T. computers bytes value, if your using {@link Value} correctly you should rarely be reading this
 * @see Value
 * @see List
 * @see Tuple
 * @see Table
 * @see Function
 * @see Exception
 */
public class Bytes {
    private byte[] data;
    public Bytes(byte[] data){
        this.data = data;
    }

    public byte[] getData(){
        return data;
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof String str){
            return str.getBytes().equals(data);
        }else if (obj instanceof Bytes bytes){
            return bytes.data.equals(data);
        }
        return super.equals(obj);
    }
}
