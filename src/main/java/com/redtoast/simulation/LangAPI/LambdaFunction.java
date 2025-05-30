package com.redtoast.simulation.LangAPI;

import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.Parameter.ParameterRules;

public interface LambdaFunction {
    Value main(FunctionInput parameters);
    ParameterRules getRules();
}