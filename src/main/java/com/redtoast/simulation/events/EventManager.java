package com.redtoast.simulation.events;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventManager {
    private static final int maxQueueSize = 75;

    private final EventQueue[] eventQueues = new EventQueue[EventLabel.values().length];
    private final EventQueue allQueue = new EventQueue();
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
        allQueue.add(ePackage);
        if (allQueue.size()>maxQueueSize) allQueue.poll();
    }

    public List<EventGeneric> readQueue(EventLabel queue) {
        return eventQueues[queue.ordinal()].readAll();
    }

    public List<EventGeneric> getQueue(EventLabel queue) {
        List<EventGeneric> buffer = readQueue(queue);
        eventQueues[queue.ordinal()].clear();
        allQueue.execute(() -> {
            for (int i = allQueue.size()-1; i > 0; i--){
                if (allQueue.get(i).label==queue) allQueue.remove(i);
            }
        });
        return buffer;
    }

    public List<EventGeneric> readQueue(EventLabel queue, String filter) {
        return eventQueues[queue.ordinal()].readAll(filter);
    }

    public List<EventGeneric> getQueue(EventLabel queue, String filter) {
        return eventQueues[queue.ordinal()].getAndRetract(filter, allQueue);
    }

    public List<EventGeneric> readAllQueue(){
        return allQueue.readAll();
    }

    public List<EventGeneric> getAllQueue(){
        List<EventGeneric> buffer = readAllQueue();
        reset();
        return buffer;
    }

    public EventGeneric getFirst(EventLabel queue) {
        if (!eventQueues[queue.ordinal()].isEmpty()) {
            EventPackage ePackage = eventQueues[queue.ordinal()].poll();
            allQueue.remove(ePackage);
            return ePackage.event;
        }
        return null;
    }

    public EventGeneric getFirst(EventLabel queue, String filter) {
        EventPackage ePackage = eventQueues[queue.ordinal()].getFirst(filter);
        if (ePackage==null) return null;
        allQueue.remove(ePackage);
        return ePackage.event;
    }

    public List<EventGeneric> readAllQueue(String filter) {
        return allQueue.readAll(filter);
    }

    public List<EventGeneric> getAllQueue(String filter) {
        LinkedList<EventGeneric> buffer = new LinkedList<>();
        LinkedList<Integer> hitlist = new LinkedList<>();
        Hashtable<EventLabel, LinkedList<Integer>> idSweep = new Hashtable<>();
        allQueue.execute(() -> {
            for (int i = 0; i < allQueue.size(); i++){
                EventPackage ePackage =allQueue.get(i);
                if (ePackage.event.getName().equals(filter)) {
                    buffer.add(ePackage.event);
                    if (idSweep.containsKey(ePackage.label)) idSweep.put(ePackage.label, new LinkedList<>());
                    idSweep.get(ePackage.label).add(ePackage.id());
                    hitlist.add(i);
                }
            }
            for (int i = hitlist.size()-1; i > 0; i--) allQueue.remove((int) hitlist.get(i));
        });
        idSweep.forEach((label, idMiniSweep) -> eventQueues[label.ordinal()].execute(() -> {
            int progress = 0;
            for (int i = eventQueues[label.ordinal()].size()-1; i > 0; i--){
                if (eventQueues[label.ordinal()].get(i).hasID(idMiniSweep)) {
                    eventQueues[label.ordinal()].remove(i);
                    progress++;
                }
                if (progress==idSweep.size()) i=0;
            }
        }));
        return buffer;
    }

    public EventGeneric getAllFirst() {
        EventPackage ePackage = allQueue.poll();
        if (ePackage==null) return null;
        return eventQueues[ePackage.label.ordinal()].execute(() -> {
            for (int i = 0; i < eventQueues[ePackage.label.ordinal()].size(); i++) {
                if (eventQueues[ePackage.label.ordinal()].get(i).equals(ePackage)) {
                    eventQueues[ePackage.label.ordinal()].remove(i);
                    return ePackage.event;
                }
            }
            return ePackage.event;
        });
    }

    public EventGeneric getAllFirst(String filter) {
        EventPackage ePackage = allQueue.getFirst(filter);
        if (ePackage==null) return null;
        return eventQueues[ePackage.label.ordinal()].execute(() -> {
            for (int i = 0; i < eventQueues[ePackage.label.ordinal()].size(); i++) {
                if (eventQueues[ePackage.label.ordinal()].get(i).equals(ePackage)) {
                    eventQueues[ePackage.label.ordinal()].remove(i);
                    return ePackage.event;
                }
            }
            return ePackage.event;
        });
    }

    public void reset() {
        for (EventLabel label : EventLabel.values()) eventQueues[label.ordinal()].clear();
        allQueue.clear();
        rollingID = 0;
    }

    protected record EventPackage(EventGeneric event, EventLabel label, int id) {
        public boolean hasID(LinkedList<Integer> sweep){
            for (Integer id2 : sweep){
                if (id2 == id){
                    return true;
                }
            }
            return false;
        }

        public EventGeneric getEvent() {return event;}
        public EventLabel getLabel() {return label;}

        @Override
        public boolean equals(Object obj){
            if (obj instanceof EventPackage ePackage) return ePackage.id == id;
            return false;
        }
    }
}
