package com.redtoast.simulation.config;

import org.joml.Vector2i;

public interface ComputerConfig {

    boolean doesBinaryGraphics();
    boolean doesColorGraphics();

    Vector2i ColorGraphicsSize();
    Vector2i BinaryGraphicsSize();

    String modelName();

    int instructionsPerBatch();
    int batchesPerTick();
}
