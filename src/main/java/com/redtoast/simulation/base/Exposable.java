package com.redtoast.simulation.base;

import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.lang.reflect.Method;

public interface Exposable {
    default void onCall(LangThread thread, Method method){}
    default Table postProcessing(Table self){
        return self;
    }
    default boolean cacheSelf(){
        return APILoader.preemptiveCache(this.getClass());
    }
}
