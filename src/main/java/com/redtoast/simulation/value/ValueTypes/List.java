package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.ast.Str;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * represents an N.E.E.T. computers list, interchangeable with {@link Tuple}
 * @see Value
 * @see Tuple
 * @see Table
 * @see Function
 * @see Exception
 * @see java.util.LinkedList
 */
public class List implements Collection<Value>, Set<Value> {
    public interface listCheck{
        boolean check(Value value);
    }
    public interface listCast<T>{
        T cast(Value value);
    }

    private java.util.List<Value> vals;
    public List(Value<?>[] values){
        vals = new LinkedList<>(Arrays.stream(values).toList());
    }
    public List(LinkedList<Value> values){
        vals = values;
    }
    public List(Object... values){
        vals = new LinkedList<>(Arrays.stream(Value.of(values).toList().toArray()).toList());
    }
    public List(){vals = new LinkedList<>();}

    public boolean check(listCheck checker){
        AtomicBoolean check = new AtomicBoolean(true);
        vals.forEach((val) -> {
            if (!checker.check(val)) check.set(false);
        });
        return check.get();
    }

    public <T> java.util.List<T> cast(listCast<T> caster){
        LinkedList<T> list = new LinkedList<>();
        vals.forEach((value) -> {
            list.add(caster.cast(value));
        });
        return list;
    }

    @Override
    public boolean remove(Object o) {
        return vals.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return vals.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Value> c) {
        return vals.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return vals.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return vals.retainAll(c);
    }

    @Override
    public void clear() {
        vals.clear();
    }

    public Value get(int index){
        return vals.get(index);
    }
    public void set(int index, Value value){
        vals.set(index, value);
    }
    public int size(){
        return vals.size();
    }

    @Override
    public boolean isEmpty() {
        return vals.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return vals.contains(o);
    }

    @NotNull
    @Override
    public Iterator<Value> iterator() {
        return vals.iterator();
    }

    public boolean isPacked(){return false;}
    public Value<List> asValue(){
        return Value.of(this);
    }
    public Value @NotNull [] toArray(){
        return vals.toArray(new Value[]{});
    }

    @NotNull
    @Override
    @Deprecated
    public <T> T @NotNull [] toArray(@NotNull T[] a) {
        return a;
    }

    @Override
    public boolean add(Value value){
        vals.add(value);
        return true;
    }

    public boolean addFirst(Value value){
        vals.addFirst(value);
        return true;
    }

    public void removeFirst(){
        vals.removeFirst();
    }

    @Override
    public String toString(){
        if (vals.isEmpty()) return "[]";
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        for (Value value : vals){
            buffer.append(value.getValue().toString());
            buffer.append(',');
        }
        buffer.setCharAt(buffer.length()-1, ']');
        return buffer.toString();
    }

    public List toList(){
        if (isPacked()){
            return new List(toArray());
        }else{
            return this;
        }
    }
    public Tuple toTuple(){
        if (isPacked()){
            return (Tuple) this;
        }else{
            return new Tuple(toArray());
        }
    }

    public List duplicate(){
        List clone = new List();
        clone.vals.addAll(vals);
        return clone;
    }
}