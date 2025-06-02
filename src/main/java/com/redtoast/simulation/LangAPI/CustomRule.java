package com.redtoast.simulation.LangAPI;

import com.redtoast.simulation.LangAPI.Parameter.LambdaRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CustomRule {
    public Class<? extends CustomParameter> rule();
}
