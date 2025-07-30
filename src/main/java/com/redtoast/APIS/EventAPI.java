package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.simulation.EventGeneric;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.value.ValueTypes.List;

import java.util.Hashtable;
import java.util.LinkedList;

public class EventAPI implements API {
    private final Computer computer;
    private final Hashtable<String, LinkedList<Function>> callbackTable = new Hashtable<>();
    private final LinkedList<Function> unmappedCallbacks = new LinkedList<>();
    private final List eventQueue = new List();
    private boolean clearQueue = false;

    public EventAPI(Computer computer){
        this.computer = computer;
        computer.addEventCallback(this::internalOnEvent);
    }

    public void internalOnEvent(EventGeneric eventGeneric){
        for (Function function : unmappedCallbacks){
            try{
                function.call(new FunctionInput(new LinkedList<>(eventGeneric.asValue().toList())));
            }catch (Exception ignored){
                Function.logError(ignored.toString());
            }
        }
        if (callbackTable.containsKey(eventGeneric.getName())){
            for (Function function : callbackTable.get(eventGeneric.getName())){
                try{
                    function.call(new FunctionInput(new LinkedList<>(eventGeneric.asValue().toTuple())));
                }catch (Exception ignored){
                    Function.logError(ignored.toString());
                }
            }
        }
        if (clearQueue) {
            clearQueue = false;
            eventQueue.clear();
        }
        if (eventQueue.size()>=100){
            eventQueue.removeFirst();
        }
        eventQueue.add(eventGeneric.asValue());
    }

    @Override
    public String getLabel() {
        return "event";
    }

    @Exposed
    public List getEventQueue(){
        return eventQueue;
    }

    @Exposed
    public int clearEventQueue(){
        int size = eventQueue.size();
        eventQueue.clear();
        return size;
    }

    @Exposed
    public void queueEvent(String eventName, Value<?>... args){
        computer.getEventQue().add(new EventGeneric(eventName, new List(args)));
    }

    @Exposed
    public void registerEvent(String event, Function function){
        if (!callbackTable.contains(event)) callbackTable.put(event, new LinkedList<>());
        callbackTable.get(event).add(function);
    }

    @Exposed
    public void registerEvent(Function function){
        unmappedCallbacks.add(function);
    }
}
