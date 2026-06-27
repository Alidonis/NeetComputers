package com.redtoast.simulation.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class EventQueue {
    protected final LinkedList<EventManager.EventPackage> queue = new LinkedList<>();

    @FunctionalInterface
    public interface Callable {
        void call();
    }

    public synchronized<T> T execute(Supplier<T> function) {
        return function.get();
    }

    public void execute(Callable function) {
        execute(() -> {
            function.call();
            return null;
        });
    }

    protected int size() {
        return execute(queue::size);
    }

    protected EventManager.EventPackage get(int index) {
        return execute(() -> queue.get(index));
    }

    protected EventManager.EventPackage remove(int index) {
        return execute(() -> queue.remove(index));
    }

    protected EventManager.EventPackage poll() {
        return queue.isEmpty() ? null : execute(queue::removeFirst);
    }

    protected void clear() {
        execute(queue::clear);
    }

    protected void add(EventManager.EventPackage eventPackage) {
        execute(() -> queue.add(eventPackage));
    }

    protected boolean isEmpty() {
        return size()==0;
    }

    private void readAll(List<EventGeneric> output) {
        execute(() -> {
            for (EventManager.EventPackage ePackage : queue){
                output.add(ePackage.getEvent());
            }
        });
    }

    protected List<EventGeneric> readAll() {
        List<EventGeneric> buffer = new ArrayList<>();
        readAll(buffer);
        return buffer;
    }

    private void readAll(List<EventGeneric> output, String filter) {
        execute(() -> {
            for (EventManager.EventPackage ePackage : queue){
                if (ePackage.getEvent().getName().equals(filter)) output.add(ePackage.getEvent());
            }
        });
    }

    protected List<EventGeneric> readAll(String filter) {
        List<EventGeneric> buffer = new ArrayList<>();
        readAll(buffer, filter);
        return buffer;
    }

    protected EventManager.EventPackage getFirst(String filter) {
        return execute(() -> {
            for (int i = 0; i < queue.size(); i++){
                EventManager.EventPackage ePackage = queue.get(i);
                if (ePackage.getEvent().getName().equals(filter)) {
                    return queue.remove(i);
                }
            }
            return null;
        });
    }

    protected void remove(EventManager.EventPackage eventPackage) {
        execute(() -> queue.remove(eventPackage));
    }

    protected List<EventGeneric> getAndRetract(String filter) {
        return execute(() -> {
            LinkedList<EventGeneric> buffer = new LinkedList<>();
            LinkedList<Integer> hitlist = new LinkedList<>();
            for (int i = 0; i < queue.size(); i++){
                EventManager.EventPackage ePackage = queue.get(i);
                if (ePackage.getEvent().getName().equals(filter)) {
                    buffer.add(ePackage.getEvent());
                    hitlist.add(i);
                }
            }
            for (int i = hitlist.size()-1; i > 0; i--) queue.remove(hitlist.get(i));
            return buffer;
        });
    }
}
