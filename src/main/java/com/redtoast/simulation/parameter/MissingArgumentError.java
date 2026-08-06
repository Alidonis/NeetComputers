package com.redtoast.simulation.parameter;

public class MissingArgumentError extends ParameterException {
    private final Parameters.ParameterType type;

    public MissingArgumentError(int position, Parameters.ParameterType type) {
        super(position);
        this.type = type;
    }

    public Parameters.ParameterType getType() {
        return type;
    }
}