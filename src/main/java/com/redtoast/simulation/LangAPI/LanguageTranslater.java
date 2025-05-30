package com.redtoast.simulation.LangAPI;

public interface LanguageTranslater<ToType, FromType, Environment> {
    public Value<?> toValue(FromType var);
    public ToType fromValue(Value<?> var);
    public void InductAPI(LangAPI API, Environment Env);
}