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

    public EventAPI(Computer computer){
        this.computer = computer;
        computer.addEventCallback(this::internalOnEvent);
    }

    public void internalOnEvent(EventGeneric eventGeneric){
        for (Function function : unmappedCallbacks){
            try{
                function.invoke(new FunctionInput(new LinkedList<>(eventGeneric.asValue().toList())));
            }catch (Exception ignored){
                Function.logError(ignored.toString());
            }
        }
        if (callbackTable.containsKey(eventGeneric.getName())){
            for (Function function : callbackTable.get(eventGeneric.getName())){
                try{
                    function.invoke(new FunctionInput(new LinkedList<>(eventGeneric.asValue().toTuple())));
                }catch (Exception ignored){
                    Function.logError(ignored.toString());
                }
            }
        }
    }

    @Override
    public String getLabel() {
        return "event";
    }

    /**
     * Manually inject an event into the queue next tick
     * @param eventName the label the injected event goes by
     * @param args a series of arguments the event will contain as data
     */
    @Exposed
    public void queueEvent(String eventName, Value<?>... args){
        computer.getEventQue().add(new EventGeneric(eventName, new List(args)));
    }

    /**
     * Before each tick, every instance of the <i>eventName</i> will be passed as a list into the <i>function</i>, the function will be called with the event name and parameters provided individually
     * @param event the label of the event that's being tracked
     * @param function the function the event is going to be forwarded to
     */
    @Exposed
    public void registerEvent(String event, Function function){
        if (!callbackTable.contains(event)) callbackTable.put(event, new LinkedList<>());
        callbackTable.get(event).add(function);
    }

    /**
     * Before each tick, every instance of any event will be passed as a list into the <i>function</i>, unlike registerEvent(event, function) the event is passed as a list. also keep in mind the event queue is capped at 100 events and will start forgetting old events if that cap is exited
     * @param function the function all events are going to be forwarded to
     */
    @Exposed
    public void registerEvent(Function function){
        unmappedCallbacks.add(function);
    }
}
