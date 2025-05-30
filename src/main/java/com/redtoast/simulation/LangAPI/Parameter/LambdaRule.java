package com.redtoast.simulation.LangAPI.Parameter;

import com.redtoast.simulation.LangAPI.Value;

public interface LambdaRule{
    boolean rule(Value arg);
    String getName();
}