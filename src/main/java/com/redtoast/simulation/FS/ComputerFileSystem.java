package com.redtoast.simulation.FS;

import java.util.UUID;

public class ComputerFileSystem extends DiskManager{
    private final DiskSystem homedisk;

    public ComputerFileSystem(int homePointer, UUID computerID) throws DiskError {
        super();
        homedisk = createDisk(homePointer, "neetos", computerID);
        if (!homedisk.isBootable()) throw new DiskError("System not bootable");
    }

    public DiskSystem getHomeDisk() {return homedisk;}

    @Override
    public boolean removeDisk(DiskSystem disk) {
        if (disk.equals(homedisk)) return false;
        return super.removeDisk(disk);
    }
}
