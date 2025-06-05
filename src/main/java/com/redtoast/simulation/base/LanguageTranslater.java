package com.redtoast.simulation.base;

import com.redtoast.simulation.value.Value;

public interface LanguageTranslater<ToType, FromType> {
    public Value<?> toValue(FromType var);
    public ToType fromValue(Value<?> var);
}