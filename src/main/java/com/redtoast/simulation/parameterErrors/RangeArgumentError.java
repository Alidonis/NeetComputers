package com.redtoast.simulation.parameterErrors;

public class RangeArgumentError extends ParameterException{
    private final int min;
    private final int max;
    private final double value;

    public RangeArgumentError(int position, int min, int max, double value) {
        super(position);
        this.min = min;
        this.max = max;
        this.value = value;
    }

    public int getMin() {return min;}
    public int getMax() {return max;}
    public double getValue() {return value;}
}
