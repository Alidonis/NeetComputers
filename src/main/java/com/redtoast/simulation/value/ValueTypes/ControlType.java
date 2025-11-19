package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.ControlCallback;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.WrappedCallback;

import java.util.UUID;

/**
 * Sets rules for how the function can return values, either simply resetting the function (calling it again instead of moving on), or setting a trigger for returning a value or running a callback (not functional)
 */
@Deprecated
public class ControlType {
    private boolean yield;
    private final ControlCallback callback;
    private final UUID uuid = UUID.randomUUID();
    private final Object triggerObj;

    public enum triggers {
        MAINTHREAD
    }

    public ControlType(){
        yield = true;
        callback = null;
        triggerObj = null;
    }

    public ControlType(ControlCallback callback, Object trigger){
        yield = false;
        this.callback = callback;
        triggerObj = trigger;
    }

    public ControlType(Object trigger, Object returnValue){
        yield = false;
        this.callback = () -> returnValue;
        triggerObj = trigger;
    }

    public WrappedCallback getCallback() {
        return yield ? null : new WrappedCallback() {
            @Override
            public UUID getUuid() {
                return uuid;
            }

            @Override
            public ControlCallback getCallback() {
                return callback;
            }

            @Override
            public void complete(Object object) {
                if (object==triggerObj) trigger();
            }
        };
    }

    public Value<?> getReturn(){
        if (isYield()) return Value.NULL;
        assert callback != null;
        return Value.of(callback.apply());
    }

    public boolean isYield() {
        return !yield;
    }

    public void trigger(){
        yield = true;
    }

    @Override
    public boolean equals(Object object){
        if (object instanceof ControlType controlType){
            if (controlType.yield && yield){
                return true;
            }else{
                return controlType.uuid == uuid;
            }
        }
        return object.equals(this);
    }
}
