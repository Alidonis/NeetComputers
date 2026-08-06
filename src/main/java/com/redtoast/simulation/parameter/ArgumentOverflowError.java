package com.redtoast.simulation.parameter;

import com.redtoast.simulation.value.VarType;

public class ArgumentOverflowError extends ParameterException {
    private final VarType user;

    public ArgumentOverflowError(int position, VarType user) {
        super(position);
        this.user = user;

    }

    public VarType getUser() {
        return user;
    }
}