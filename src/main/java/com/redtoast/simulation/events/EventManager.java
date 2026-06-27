package com.redtoast.simulation.events;

import java.util.List;

public class EventManager {
    private static final int maxQueueSize = 75;

    private final EventQueue[] eventQueues = new EventQueue[EventLabel.values().length];
    private int rollingID = 0;

    public EventManager(){
        for (EventLabel label : EventLabel.values()) eventQueues[label.ordinal()] = new EventQueue();
    }

    private int getID(){
        int temp = rollingID;
        if (rollingID==Integer.MAX_VALUE) rollingID = 0;
        rollingID++;
        return temp;
    }

    public void queueEvent(EventGeneric event, EventLabel queue) {
        if (queue==null) queue = EventLabel.UNLABELED;
        EventPackage ePackage = new EventPackage(event, queue, getID());
        eventQueues[ePackage.label.ordinal()].add(ePackage);
        if (eventQueues[ePackage.label.ordinal()].size()>maxQueueSize) eventQueues[ePackage.label.ordinal()].poll();
    }

    public List<EventGeneric> readQueue(EventLabel queue) {
        return eventQueues[queue.ordinal()].readAll();
    }

    public List<EventGeneric> getQueue(EventLabel queue) {
        List<EventGeneric> buffer = readQueue(queue);
        eventQueues[queue.ordinal()].clear();
        return buffer;
    }

    public List<EventGeneric> readQueue(EventLabel queue, String filter) {
        return eventQueues[queue.ordinal()].readAll(filter);
    }

    public List<EventGeneric> getQueue(EventLabel queue, String filter) {
        return eventQueues[queue.ordinal()].getAndRetract(filter);
    }

    public EventGeneric getFirst(EventLabel queue) {
        if (!eventQueues[queue.ordinal()].isEmpty()) {
            EventPackage ePackage = eventQueues[queue.ordinal()].poll();
            return ePackage.event;
        }
        return null;
    }

    public EventGeneric getFirst(EventLabel queue, String filter) {
        EventPackage ePackage = eventQueues[queue.ordinal()].getFirst(filter);
        if (ePackage==null) return null;
        return ePackage.event;
    }

    public void reset() {
        for (EventLabel label : EventLabel.values()) eventQueues[label.ordinal()].clear();
        rollingID = 0;
    }

    public void clear(EventLabel queue) {
        eventQueues[queue.ordinal()].clear();
    }

    protected record EventPackage(EventGeneric event, EventLabel label, int id) {
        public EventGeneric getEvent() {return event;}
        public EventLabel getLabel() {return label;}

        @Override
        public boolean equals(Object obj){
            if (obj instanceof EventPackage ePackage) return ePackage.id == id;
            return false;
        }
    }
}
