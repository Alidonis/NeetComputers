package com.redtoast.simulation.FS;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import org.jetbrains.annotations.Nullable;

public interface PartitionedFileSpace extends FileSpace{
    boolean partitionExists(String path);

    @Nullable Partition getPartition(String path);

    @Exposed(nameOverride = "getPartitions") List LangGetPartitions();

    @Exposed(nameOverride = "getPartition") public Table LangGetPartition(String name);

    @Exposed boolean createPartition(String name);

    @Exposed boolean setPartitionHidden(String name, boolean state);

    @Exposed boolean setPartitionReadOnly(String name);
}