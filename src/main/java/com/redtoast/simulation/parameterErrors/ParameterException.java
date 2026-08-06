package com.redtoast.simulation.parameterErrors;

public class ParameterException extends RuntimeException {
    private int position;

    public ParameterException(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPositionIfMissing(int position) {
        if (this.position<0) this.position = position;
    }
}
