package com.redtoast.simulation.events;

import java.util.Hashtable;
import java.util.LinkedList;

public class EventManager {
    private static final int maxQueueSize = 75;

    private final LinkedList<eventPackage>[] eventQueues = new LinkedList[EventLabel.values().length];
    private final LinkedList<eventPackage> allQueue = new LinkedList<>();
    private final LinkedList<eventPackage> entryQueue = new LinkedList<>();
    private int rollingID = 0;

    public EventManager(){
        for (EventLabel label : EventLabel.values()) eventQueues[label.ordinal()]=new LinkedList<>();
    }

    private int getID(){
        int temp = rollingID;
        if (rollingID==Integer.MAX_VALUE) rollingID = 0;
        rollingID++;
        return temp;
    }

    public void queueEvent(EventGeneric event, EventLabel queue) {
        if (queue==null) queue = EventLabel.UNLABELED;
        eventPackage ePackage = new eventPackage(event, queue, getID());
        entryQueue.add(ePackage);
    }

    public void update(){
        for (eventPackage ePackage : entryQueue){
            eventQueues[ePackage.label.ordinal()].add(ePackage);
            if (eventQueues[ePackage.label.ordinal()].size()>maxQueueSize) eventQueues[ePackage.label.ordinal()].removeFirst();
            allQueue.add(ePackage);
            if (allQueue.size()>maxQueueSize) allQueue.removeFirst();
        }
        entryQueue.clear();
    }

    public LinkedList<EventGeneric> readQueue(EventLabel queue) {
        LinkedList<EventGeneric> buffer = new LinkedList<>();
        for (eventPackage ePackage : eventQueues[queue.ordinal()]){
            buffer.add(ePackage.event);
        }
        return buffer;
    }

    public LinkedList<EventGeneric> getQueue(EventLabel queue) {
        LinkedList<EventGeneric> buffer = readQueue(queue);
        eventQueues[queue.ordinal()].clear();
        for (int i = allQueue.size()-1; i > 0; i--){
            if (allQueue.get(i).label==queue) allQueue.remove(i);
        }
        return buffer;
    }

    public LinkedList<EventGeneric> readQueue(EventLabel queue, String filter) {
        LinkedList<EventGeneric> buffer = new LinkedList<>();
        for (eventPackage ePackage : eventQueues[queue.ordinal()]){
            if (ePackage.event.getName().equals(filter)) buffer.add(ePackage.event);
        }
        return buffer;
    }

    public LinkedList<EventGeneric> getQueue(EventLabel queue, String filter) {
        LinkedList<EventGeneric> buffer = new LinkedList<>();
        LinkedList<Integer> idSweep = new LinkedList<>();
        LinkedList<Integer> hitlist = new LinkedList<>();
        for (int i = 0; i < eventQueues[queue.ordinal()].size(); i++){
            eventPackage ePackage = eventQueues[queue.ordinal()].get(i);
            if (ePackage.event.getName().equals(filter)) {
                buffer.add(ePackage.event);
                idSweep.add(ePackage.id());
                hitlist.add(i);
            }
        }
        for (int i = hitlist.size()-1; i > 0; i--) eventQueues[queue.ordinal()].remove((int) hitlist.get(i));
        int progress = 0;
        for (int i = allQueue.size()-1; i > 0; i--){
            if (allQueue.get(i).hasID(idSweep)) {
                allQueue.remove(i);
                progress++;
            }
            if (progress==idSweep.size()) return buffer;
        }
        return buffer;
    }

    public LinkedList<EventGeneric> readAllQueue(){
        LinkedList<EventGeneric> buffer = new LinkedList<>();
        for (eventPackage ePackage : allQueue){
            buffer.add(ePackage.event);
        }
        return buffer;
    }

    public LinkedList<EventGeneric> getAllQueue(){
        LinkedList<EventGeneric> buffer = readAllQueue();
        reset();
        return buffer;
    }

    public EventGeneric getFirst(EventLabel queue) {
        if (!eventQueues[queue.ordinal()].isEmpty()) {
            eventPackage ePackage = eventQueues[queue.ordinal()].removeFirst();
            for (int i = 0; i < allQueue.size(); i++) {
                if (allQueue.get(i).equals(ePackage)) {
                    allQueue.remove(i);
                    return ePackage.event;
                }
            }
            return ePackage.event;
        }
        return null;
    }

    private eventPackage getFirstFromList(String filter, LinkedList<eventPackage> queue){
        for (int i = 0; i < queue.size(); i++){
            eventPackage ePackage = queue.get(i);
            if (ePackage.event.getName().equals(filter)) {
                return queue.remove(i);
            }
        }
        return null;
    }

    public EventGeneric getFirst(EventLabel queue, String filter) {
        eventPackage ePackage = getFirstFromList(filter, eventQueues[queue.ordinal()]);
        if (ePackage==null) return null;
        for (int i = 0; i < allQueue.size(); i++) {
            if (allQueue.get(i).equals(ePackage)) {
                allQueue.remove(i);
                return ePackage.event;
            }
        }
        return ePackage.event;
    }

    public LinkedList<EventGeneric> readAllQueue(String filter) {
        LinkedList<EventGeneric> buffer = new LinkedList<>();
        for (eventPackage ePackage : allQueue){
            if (ePackage.event.getName().equals(filter)) buffer.add(ePackage.event);
        }
        return buffer;
    }

    public LinkedList<EventGeneric> getAllQueue(String filter) {
        LinkedList<EventGeneric> buffer = new LinkedList<>();
        LinkedList<Integer> hitlist = new LinkedList<>();
        Hashtable<EventLabel, LinkedList<Integer>> idSweep = new Hashtable<>();
        for (int i = 0; i < allQueue.size(); i++){
            eventPackage ePackage =allQueue.get(i);
            if (ePackage.event.getName().equals(filter)) {
                buffer.add(ePackage.event);
                if (idSweep.containsKey(ePackage.label)) idSweep.put(ePackage.label, new LinkedList<>());
                idSweep.get(ePackage.label).add(ePackage.id());
                hitlist.add(i);
            }
        }
        for (int i = hitlist.size()-1; i > 0; i--) allQueue.remove((int) hitlist.get(i));
        idSweep.forEach((label, idMiniSweep) -> {
            int progress = 0;
            for (int i = eventQueues[label.ordinal()].size()-1; i > 0; i--){
                if (eventQueues[label.ordinal()].get(i).hasID(idMiniSweep)) {
                    eventQueues[label.ordinal()].remove(i);
                    progress++;
                }
                if (progress==idSweep.size()) i=0;
            }
        });
        return buffer;
    }

    public EventGeneric getAllFirst() {
        if (!allQueue.isEmpty()) {
            eventPackage ePackage = allQueue.removeFirst();
            for (int i = 0; i < eventQueues[ePackage.label.ordinal()].size(); i++) {
                if (eventQueues[ePackage.label.ordinal()].get(i).equals(ePackage)) {
                    eventQueues[ePackage.label.ordinal()].remove(i);
                    return ePackage.event;
                }
            }
            return ePackage.event;
        }
        return null;
    }

    public EventGeneric getAllFirst(String filter) {
        eventPackage ePackage = getFirstFromList(filter, allQueue);
        if (ePackage==null) return null;
        for (int i = 0; i < eventQueues[ePackage.label.ordinal()].size(); i++) {
            if (eventQueues[ePackage.label.ordinal()].get(i).equals(ePackage)) {
                eventQueues[ePackage.label.ordinal()].remove(i);
                return ePackage.event;
            }
        }
        return ePackage.event;
    }

    public void reset() {
        for (EventLabel label : EventLabel.values()) eventQueues[label.ordinal()].clear();
        allQueue.clear();
        rollingID = 0;
    }

    private record eventPackage(EventGeneric event, EventLabel label, int id) {
        public boolean hasID(LinkedList<Integer> sweep){
            for (Integer id2 : sweep){
                if (id2 == id){
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object obj){
            if (obj instanceof eventPackage ePackage) return ePackage.id == id;
            return false;
        }
    }
}
