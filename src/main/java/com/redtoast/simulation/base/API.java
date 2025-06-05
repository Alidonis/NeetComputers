package com.redtoast.simulation.base;

public interface API {
    String getLabel();
    default void onCall(LangThread thread){}
}
