package com.redtoast.simulation.cache;

import com.redtoast.simulation.parameter.ParameterRules;

import java.lang.reflect.Method;

public record StaticFunctionCache(
        Method method,
        ParameterRules ruleset,
        String functionName
) {

}
