package com.redtoast.simulation.LangAPI;


import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.ValueTypes.Function;
import com.redtoast.simulation.LangAPI.ValueTypes.Table;

public class LangAPI {
    private final Table table;
    private final String label;

    public LangAPI(String n){
        label = n;
        table = new Table();
    }
    public void set(String n, Value value){
        table.put(n, value);
    }
    public void set(String n, LambdaFunction func){
        table.put(n, new Value<Function>(new Function(func.getRules()) {
            @Override
            public Value call(FunctionInput parameters) {
                return func.main(parameters);
            }
        }));
    }
    public void set(String n, Function func){
        table.put(n, new Value<>(func));
    }
    public Value get(String n){
        return table.get(n);
    }
    public Table getTable(){
        return table;
    }
    public String getLabel(){
        return label;
    }
}
