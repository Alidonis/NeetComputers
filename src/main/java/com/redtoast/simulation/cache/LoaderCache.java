package com.redtoast.simulation.cache;

import java.util.Collection;
import java.util.LinkedList;

public record LoaderCache(
    Collection<StaticFunctionCache> functions,
    Collection<PackedFunctionCache> packedFunctions
) {

}
