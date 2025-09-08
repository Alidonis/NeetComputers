package com.redtoast.simulation.FS;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.external.PeripheralProvider;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import net.minecraft.nbt.NbtCompound;

public interface FileSpace extends API {
    default String getLabel(){return "file system";}

    Filepath getFile(String path);

    @Exposed(nameOverride = "open") default Table openFile(String path){
        return openFile(path, "r");
    }

    @Exposed(nameOverride = "open") Table openFile(String path, String mode);

    @Exposed boolean exists(String path);

    @Exposed boolean makeDir(String path);

    @Exposed boolean isDir(String path);

    @Exposed boolean isFile(String path);

    @Exposed List getChildren(String path);

    @Exposed boolean delete(String path);

    NbtCompound saveAsNBT();
}