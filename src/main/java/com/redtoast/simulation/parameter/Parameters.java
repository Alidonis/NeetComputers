package com.redtoast.simulation.parameter;

import com.redtoast.simulation.base.LanguageGeneric;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Tuple;
import com.redtoast.simulation.value.VarType;

import java.util.Arrays;
import java.util.Optional;

public record Parameters(ParameterHelper.ParameterType[] types, Class<?>[] classes, boolean isPacked) {
    public Object[] cast(Value<?>[] values) {
        Object[] array = new Object[types.length];
        if (isPacked) {
            Tuple varargs = new Tuple();
            int i = 0;
            int x = size()-1;
            for (; i < x; i++) {
                array[i] = ParameterHelper.cast(values[i], classes[i], false, types[i].annotations());
            }
            for (; i < values.length; i++) {
                varargs.add(values[i]);
            }
            array[x] = ParameterHelper.cast(varargs.asValue(), classes[x], true, types[x].annotations());
        }else{
            for (int i = 0; i < size(); i++) {
                System.out.println(values[i].getType());
                array[i] = ParameterHelper.cast(values[i], classes[i], false, types[i].annotations());
            }
        }
        return array;
    }

    public Optional<String> canCast(Value<?>[] values, LanguageGeneric language) {
        if (language == null && values.length>0) language = values[0].getLanguage();
        int i = 0;
        int size = isPacked ? size()-1 : size();
        /*check if values can be cast*/
        for (; i < Math.min(values.length, size); i++) {
            if (!types[i].canCast(values[i])) return getError(ParameterErrorType.MISMATCHED_ARGUMENT_ERROR, language, i, values[i].getType(), types[i]);
        }
        /*throw errors for values that are missing*/
        if (i < size) return getError(ParameterErrorType.MISSING_ARGUMENT_ERROR, language, i, null, types[i]);
        if (isPacked) {
            Tuple tuple = new Tuple();
            for (; i < values.length; i++) tuple.add(values[i]);
            if (!types[size].canCast(tuple.asValue())) return getError(ParameterErrorType.MISMATCHED_VARARGS_ERROR, language, size, null, types[size]);

        }else if (values.length > size) return getError(ParameterErrorType.ARGUMENT_OVERFLOW_ERROR, language, size, values[size].getType(), null);
        return Optional.empty();
    }

    private Optional<String> getError(ParameterErrorType type, LanguageGeneric language, int position, VarType userType, ParameterHelper.ParameterType correctType) {
        return language==null ? Optional.of("Language Missing #"+position) : Optional.of(language.generateError(type, position, userType, correctType));
    }

    public int size() {return types.length;}

    public enum ParameterErrorType {
        ARGUMENT_OVERFLOW_ERROR,
        MISSING_ARGUMENT_ERROR,
        MISMATCHED_ARGUMENT_ERROR,
        MISMATCHED_VARARGS_ERROR
    }

    @Override
    public String toString() {
        return isPacked ? Arrays.toString(types).replaceFirst("\\[]]$", "...]") : Arrays.toString(types);
    }
}
