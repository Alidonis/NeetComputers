package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.events.EventGeneric;
import com.redtoast.simulation.events.EventLabel;
import com.redtoast.simulation.events.EventManager;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;

public class EventAPI implements API {
    EventManager eventManager;

    public EventAPI(Computer computer) {
        this.eventManager = computer.getEventManager();
    }

    private EventLabel decodeEventLabel(String string) {
        for (EventLabel label : EventLabel.values()) {
            if (label.name().equals(string.toUpperCase())) return label;
        }
        throw new ExposedError("Invalid event category '"+string+"'");
    }

    @Exposed
    public void queueEvent(String category, String eventName, Value<?>... args){
        eventManager.queueEvent(new EventGeneric(eventName, new List(args)), category.equalsIgnoreCase("all") ? EventLabel.UNLABELED : decodeEventLabel(category));
    }

    @Exposed
    public Value<List> getQueue(String category){
        if (category.equalsIgnoreCase("all")){
            return Value.of(eventManager.getAllQueue());
        }else{
            return Value.of(eventManager.getQueue(decodeEventLabel(category)));
        }
    }

    @Exposed
    public Value<List> getQueue(String category, String filter) {
        if (category.equalsIgnoreCase("all")){
            return Value.of(eventManager.getAllQueue(filter));
        }else{
            return Value.of(eventManager.getQueue(decodeEventLabel(category), filter));
        }
    }

    @Exposed
    public Value<List> getFirst(String category){
        if (category.equalsIgnoreCase("all")){
            return Value.of(eventManager.getAllFirst());
        }else{
            return Value.of(eventManager.getFirst(decodeEventLabel(category)));
        }
    }

    @Exposed
    public Value<List> getFirst(String category, String filter) {
        if (category.equalsIgnoreCase("all")){
            return Value.of(eventManager.getAllFirst(filter));
        }else{
            return Value.of(eventManager.getFirst(decodeEventLabel(category), filter));
        }
    }

    @Exposed
    public void clear(){
        eventManager.reset();
    }

    @Override
    public String getLabel() {
        return "event";
    }
}
