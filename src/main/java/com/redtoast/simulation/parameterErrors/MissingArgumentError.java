package com.redtoast.simulation.parameterErrors;

import com.redtoast.simulation.Parameters;

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