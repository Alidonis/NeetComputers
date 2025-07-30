package com.redtoast.simulation.base;

public interface Exposable {
    default void onCall(LangThread thread){}
}
