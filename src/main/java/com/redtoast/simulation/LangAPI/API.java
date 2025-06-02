package com.redtoast.simulation.LangAPI;

import com.redtoast.simulation.LangAPI.ValueTypes.Function;

import java.util.LinkedList;

public interface API {
    LinkedList<Function> runtimeFunctions = new LinkedList<>();
    String getLabel();
}
