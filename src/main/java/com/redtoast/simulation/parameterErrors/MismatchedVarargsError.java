package com.redtoast.simulation.parameterErrors;

import com.redtoast.simulation.Parameters;
import com.redtoast.simulation.value.VarType;

public class MismatchedVarargsError extends ParameterException {
    private final VarType user;
    private final Parameters.ParameterType type;

    public MismatchedVarargsError(int position, VarType user, Parameters.ParameterType type) {
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
