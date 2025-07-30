package com.redtoast.simulation.FS;

import com.redtoast.simulation.base.LanguageGeneric;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface BootableFilespace extends FileSpace{
    record BootPath(Filepath entryPoint, LanguageGeneric language){ }
    @NotNull BootPath fetchBootPath();
    boolean canBoot() throws IOException;
}
