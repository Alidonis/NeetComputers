package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.value.NVTable;
import com.redtoast.simulation.value.Value;

public class Flash implements API {
    NVTable nv;
    @Override
    public String getLabel() {
        return "flash";
    }

    public Flash(Computer computer){
        nv = computer.getNVRam();
    }

    /**
     * Fetches data from flash memory or returns null
     * @param key the key to fetch
     * @return the fetched value
     */
    @Exposed
    public Value get(String key){
        return nv.getOrDefault(key, Value.NULL);
    }

    /**
     * Fetches data from flash memory or sets the value if it does not exist
     * @param key key to fetch / set
     * @param _default default value to set if null
     * @return fetched or set value
     */
    @Exposed
    public Value get(String key, Value _default){
        if (nv.containsKey(key)){
            return nv.get(key);
        }else{
            nv.put(key, _default);
            return _default;
        }
    }

    /**
     * Sets the value of a location in flash memory
     * @param key location to override
     * @param value value to set
     * @return the set value
     */
    @Exposed
    public Value set(String key, Value value){
        nv.put(key, value);
        return value;
    }
}
