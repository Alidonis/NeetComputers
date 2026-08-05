package com.redtoast.simulation.cache;

import com.redtoast.simulation.parameter.Parameters;

import java.lang.reflect.Method;

public record StaticFunctionCache(
        Method method,
        Parameters ruleset,
        String functionName
) {

}
