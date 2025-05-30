package com.redtoast.simulation.LangAPI.Parameter;

public class ParameterCheckReturn {
    private boolean isError = false;
    private String error;
    private FunctionInput values;
    public ParameterCheckReturn(String message){
        isError = true;
        error = message;
    }
    public ParameterCheckReturn(FunctionInput data){
        values = data;
    }
    public boolean isError(){
        return isError;
    }
    public String getMessage(){
        return error;
    }
    public FunctionInput getFunctionInput(){
        return values;
    }
}