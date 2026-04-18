package com.redtoast.simulation.config;

import org.joml.Vector2i;

public class DefaultComputerConfig implements ComputerConfig{
    private Vector2i binaryGraphicsSize = null;
    private final String modelName;

    public DefaultComputerConfig(String modelName) {
        this.modelName = modelName;
    }

    public DefaultComputerConfig(String modelName, int binaryScreenSizeX, int binaryScreenSizeY) {
        this.modelName = modelName;
        binaryGraphicsSize = new Vector2i(binaryScreenSizeX, binaryScreenSizeY);
    }

    @Override
    public boolean doesBinaryGraphics() {
        return binaryGraphicsSize!=null;
    }

    @Override
    public boolean doesColorGraphics() {
        return true;
    }

    @Override
    public Vector2i ColorGraphicsSize() {
        return new Vector2i(384, 288);
    }

    @Override
    public Vector2i BinaryGraphicsSize() {
        return binaryGraphicsSize;
    }

    @Override
    public String modelName() {
        return modelName;
    }

    @Override
    public int instructionsPerBatch() {
        return 10;
    }

    @Override
    public int batchesPerTick() {
        return 25000;
    }
}
