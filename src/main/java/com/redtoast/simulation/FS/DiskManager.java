package com.redtoast.simulation.FS;

import com.redtoast.neet.NeetComputersServer;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DiskManager {
    private final Map<Integer, DiskSystem> disks = new ConcurrentHashMap<>();

    public DiskSystem getDisk(int disk) {
        return disks.get(disk);
    }

    public boolean removeDisk(DiskSystem disk) {
        for (Map.Entry<Integer, DiskSystem> entry : disks.entrySet()) {
            if (entry.getValue().equals(disk)) {
                disks.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    public boolean addDisk(DiskSystem disk) {
        boolean check = false;
        for (Map.Entry<Integer, DiskSystem> entry : disks.entrySet()) {
            if (entry.getValue().equals(disk)) {
                check = true;
            }
        }
        if (check){
            return false;
        }else{
            int place = 0;
            while (disks.containsKey(place)) {
                place++;
            }
            disks.put(place, disk);
            return true;
        }
    }

    public DiskSystem createDisk(int pointer, String template, UUID uuid) throws DiskError {
        Path path = NeetComputersServer.worldPath.resolve("neetcomputers").resolve(String.valueOf(pointer)).normalize();
        for (Map.Entry<Integer, DiskSystem> entry : disks.entrySet()) {
            if (entry.getValue().basePath.equals(path)) throw new DiskError("Disk already exists in system");
        }
        DiskSystem disk = new DiskSystem(pointer, template, uuid);
        addDisk(disk);
        return disk;
    }

    public int size(){
        return disks.size();
    }

    public int[] diskNumbers(){
        int[] arr = new int[size()];
        int place = 0;
        for (Map.Entry<Integer, DiskSystem> entry : disks.entrySet()) {
            arr[place] = entry.getKey();
            place++;
        }
        return arr;
    }

    public void update(){
        for (Map.Entry<Integer, DiskSystem> entry : disks.entrySet()) entry.getValue().update();
    }
}
