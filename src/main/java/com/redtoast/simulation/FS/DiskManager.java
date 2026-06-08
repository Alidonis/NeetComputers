package com.redtoast.simulation.FS;

import com.redtoast.neet.NeetComputersServer;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DiskManager {
    private final List<DiskSystem> disks = new LinkedList<>();

    public DiskSystem getDisk(int disk) {
        return disks.get(disk);
    }

    public boolean removeDisk(DiskSystem disk) {
        return disks.remove(disk);
    }

    public boolean addDisk(DiskSystem disk) {
        if (disks.contains(disk)){
            return false;
        }else{
            disks.add(disk);
            return true;
        }
    }

    public DiskSystem createDisk(int pointer, String template, UUID uuid) throws DiskError {
        Path path = NeetComputersServer.worldPath.resolve("neetcomputers").resolve(String.valueOf(pointer)).normalize();
        for (DiskSystem disk : disks) {
            if (disk.basePath.equals(path)) throw new DiskError("Disk already exists in system");
        }
        DiskSystem disk = new DiskSystem(pointer, template, uuid);
        disks.add(disk);
        return disk;
    }

    public int size(){
        return disks.size();
    }

    public void update(){
        for (DiskSystem disk : disks) disk.update();
    }
}
