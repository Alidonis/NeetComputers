package com.redtoast.APIS;

import com.redtoast.Computer;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.FS.*;
import com.redtoast.simulation.FS.FileImplementations.Filepath;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.util.LinkedList;

public class FilesAPI implements API {
    private final DiskManager diskManager;
    private final Computer parent;

    public FilesAPI(Computer computer) {
        diskManager = computer.getFileSystem();
        parent = computer;
    }

    public GenericSystem getDisk(int disk) {
        if (disk < 0 || disk >= diskManager.size()) throw new ExposedError("Disk not found");
        return diskManager.getDisk(disk);
    }

    @Exposed
    public List getPartitions(int disk){
        GenericSystem diskSystem = getDisk(disk);
        LinkedList<Value<String>> partitionsStrings = new LinkedList<>();
        for (Partition partition : diskSystem.getPartitions()){
            partitionsStrings.add(Value.of(partition.path()));
        }
        return new List(partitionsStrings);
    }

    @Exposed
    public List getPartitions(){
        return getPartitions(0);
    }

    @Exposed
    public Table getPartition(String name, int disk){
        GenericSystem diskSystem = getDisk(disk);
        Partition partition = diskSystem.getPartition(name);
        if (partition==null) return null;
        Table table = new Table();
        table.put("name", partition.path());
        table.put("readonly", Value.of(partition.readOnly()));
        table.put("hidden", Value.of(partition.hidden()));
        return table;
    }

    @Exposed
    public Table getPartition(String name){
        return getPartition(name, 0);
    }

    @Exposed
    public boolean createPartition(String name, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.createPartition(name);
    }

    @Exposed
    public boolean createPartition(String name){
        return createPartition(name, 0);
    }

    @Exposed
    public boolean setPartitionHidden(String name, boolean state, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.setPartitionHidden(name, state);
    }

    @Exposed
    public boolean setPartitionHidden(String name, boolean state){
        return setPartitionHidden(name, state, 0);
    }

    @Exposed
    public boolean setPartitionReadOnly(String name, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.setPartitionReadOnly(name, true);
    }

    @Exposed
    public boolean deletePartition(String name){
        return deletePartition(name, 0);
    }

    @Exposed
    public boolean deletePartition(String name, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.deletePartition(name);
    }

    @Exposed
    public boolean setPartitionReadOnly(String name){
        return setPartitionReadOnly(name, 0);
    }

    @Exposed
    public Table open(String path){
        return open(path, "r", 0);
    }

    @Exposed
    public Table open(String path, int disk){
        return open(path, "r", disk);
    }

    @Exposed
    public Table open(String path, String mode){
        return open(path, mode, 0);
    }

    @Exposed
    public Table open(String path, String mode, int disk){
        GenericSystem diskSystem = getDisk(disk);
        OpeningMode openingMode = FileHelper.getMode(mode);
        Filepath filepath = diskSystem.getFile(path);
        if (filepath.isDirectory()) throw new ExposedError("Not a file");
        if (openingMode.invalid()) throw new ExposedError("Invalid open mode");
        if (!filepath.exists() && !openingMode.create()) throw new ExposedError("Not a file");
        if (openingMode.canRead() && !filepath.canRead()) throw new ExposedError("Access denied");
        if (openingMode.canWrite() && !filepath.canWrite()) throw new ExposedError("Access denied");
        return APILoader.TableizeAPI(new FileHeader(filepath, diskSystem, openingMode), parent.getRuntime());
    }

    @Exposed
    public List getChildren(String path, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.getChildren(path);
    }

    @Exposed
    public List getChildren(String path){
        return getChildren(path, 0);
    }

    @Exposed
    public boolean makeDir(String path, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.makeDir(path);
    }

    @Exposed
    public boolean makeDir(String path){
        return makeDir(path, 0);
    }

    @Exposed
    public boolean exists(String path, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.exists(path);
    }

    @Exposed
    public boolean exists(String path){
        return exists(path, 0);
    }

    @Exposed
    public boolean isFile(String path, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.isFile(path);
    }

    @Exposed
    public boolean isFile(String path){
        return isFile(path, 0);
    }

    @Exposed
    public boolean isDir(String path, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.isDir(path);
    }

    @Exposed
    public boolean isDir(String path){
        return isDir(path, 0);
    }

    @Exposed
    public boolean delete(String path, int disk){
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.delete(path);
    }

    @Exposed
    public boolean delete(String path){
        return delete(path, 0);
    }

    @Exposed
    public int getNumberOfDisks() {
        return diskManager.size();
    }

    @Exposed
    public int[] getDisks(){return diskManager.diskNumbers();}

    @Exposed
    public String getDiskID(int disk) {
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.getUuid().toString();
    }

    @Exposed
    public boolean removeDisk(int disk) {
        GenericSystem diskSystem = getDisk(disk);
        return diskManager.removeDisk(diskSystem);
    }

    @Exposed
    public String getBootPath(int disk) {
        GenericSystem diskSystem = getDisk(disk);
        return diskSystem.isBootable() ? FileHelper.normalize(diskSystem.getEntrypointPath()) : null;
    }

    @Exposed
    public boolean setBoot(String entrypoint, int disk) {
        GenericSystem diskSystem = getDisk(disk);
        if (disk==0 && !exists(entrypoint)) {
            return false;
        }
        try{
            diskSystem.makeBootable(entrypoint, parent.getHomeDiskSystem().getLanguage());
        } catch (DiskError e) {
            if (e.getMessage().equals("Disk not bootable")){
                parent.crash("Failed to load file system: Disk not bootable");
                return false;
            }else{
                throw new ExposedError(e.getMessage());
            }
        }
        return true;
    }

    @Exposed
    public boolean setBoot(int disk) {
        GenericSystem diskSystem = getDisk(disk);
        if (disk==0) {
            return false;
        }
        boolean buffer = diskSystem.isBootable();
        diskSystem.removeBootability();
        return buffer;
    }

    @Override
    public String getLabel() {
        return "files";
    }
}
