package com.redtoast.simulation.parameter;

import com.redtoast.simulation.value.VarType;

public class MismatchedArgumentError extends ParameterException {
    private final VarType user;
    private final Parameters.ParameterType type;

    public MismatchedArgumentError(int position, VarType user, Parameters.ParameterType type) {
        super(position);
        this.user = user;
        this.type = type;
    }

    public VarType getUser() {
        return user;
    }

    public Parameters.ParameterType getType() {
        return type;
    }
}