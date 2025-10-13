package com.redtoast.simulation.FS;

import com.redtoast.Computer;
import com.redtoast.external.PeripheralConsumer;
import com.redtoast.external.PeripheralProvider;
import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.FS.builder.FileContext;
import com.redtoast.simulation.FS.builder.SystemBuild;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public class FileSystem implements BootablePartitionedFileSpace, PeripheralProvider {
    public final SystemBuild build;
    public final int pointer;
    public final Computer parent;
    public final Path basePath;
    public final Hashtable<UUID, FileHeader> map = new Hashtable<>();

    public FileSystem(SystemBuild build, int pointer, Computer parent) {
        Path path;
        this.build = build;
        this.pointer = pointer;
        path = NeetComputers.worldPath.resolve("neetcomputers").resolve(String.valueOf(pointer));
        path = path.normalize();
        basePath = path;
        basePath.toFile().mkdir();
        for (Partition partition : build.partitions){
            basePath.resolve(partition.path()).toFile().mkdir();
        }
        this.parent = parent;
    }

    public FileSystem(SystemBuild build, int pointer, FileContext context, Computer parent){
        this(build, pointer, parent);
    }

    public Filepath getFile(String path){
        return FileHelper.getFile(this, path);
    }

    public boolean partitionExists(String path){
        for (Partition partition : build.partitions){
            if (partition.path().equals(path)){
                return true;
            }
        }
        return false;
    }

    public @Nullable Partition getPartition(String path){
        for (Partition partition : build.partitions){
            if (partition.path().equals(path)){
                return partition;
            }
        }
        return null;
    }

    @Exposed(nameOverride = "getPartitions")
    public List LangGetPartitions(){
        LinkedList<Value<Table>> partitionsStrings = new LinkedList<>();
        for (Partition partition : build.partitions){
            Table table = new Table();
            table.put("name", partition.path());
            table.put("readonly", Value.of(partition.readOnly()));
            table.put("hidden", Value.of(partition.hidden()));
            partitionsStrings.add(table.asValue());
        }
        return new List(partitionsStrings);
    }

    @Exposed(nameOverride = "getPartition")
    public Table LangGetPartition(String name){
        Partition partition = getPartition(name);
        if (partition==null) return null;
        Table table = new Table();
        table.put("name", partition.path());
        table.put("readonly", Value.of(partition.readOnly()));
        table.put("hidden", Value.of(partition.hidden()));
        return table;
    }

    @Exposed
    public boolean createPartition(String name){
        boolean marker = FileHelper.validatePathStatic(name+":\\");
        if (!marker) return false;
        if (getPartition(name)!=null) return false;
        basePath.resolve(name).toFile().mkdir();
        build.partitions.add(new Partition(name, false, false, "0"));
        return true;
    }

    @Exposed
    public boolean setPartitionHidden(String name, boolean state){
        for (int i = 0; i < build.partitions.size(); i++){
            if (build.partitions.get(i).path().equals(name)){
                build.partitions.set(i, new Partition(name, build.partitions.get(i).readOnly(), state, build.partitions.get(i).source()));
                return true;
            }
        }
        return false;
    }

    @Exposed
    public boolean setPartitionReadOnly(String name){
        for (int i = 0; i < build.partitions.size(); i++){
            if (build.partitions.get(i).path().equals(name)){
                build.partitions.set(i, new Partition(name, true, build.partitions.get(i).hidden(), build.partitions.get(i).source()));
                return true;
            }
        }
        return false;
    }

    @Exposed(nameOverride = "open")
    public Table openFile(String path){
        return openFile(path, "r");
    }

    @Exposed(nameOverride = "open")
    public Table openFile(String path, String mode){
        OpeningMode openingMode = FileHelper.getMode(mode);
        Filepath filepath = getFile(path);
        if (filepath.isDirectory()) throw new ExposedError("Not a file");
        if (openingMode.invalid()) throw new ExposedError("Invalid open mode");
        if (!filepath.exists() && !openingMode.create()) throw new ExposedError("Not a file");
        if (openingMode.canRead() && !filepath.canRead()) throw new ExposedError("Access denied");
        if (openingMode.canWrite() && !filepath.canWrite()) throw new ExposedError("Access denied");
        return LangizeFile(filepath, openingMode);
    }

    @Exposed
    public boolean exists(String path){
        Filepath file = getFile(path);
        return file.exists();
    }

    @Exposed
    public boolean makeDir(String path){
        Filepath file = getFile(path);
        if (!file.canWrite()) throw new ExposedError("Access denied");
        try{
            return file.mkdirs();
        }catch (Exception exception){
            if (exception instanceof IOException){
                throw new ExposedError(exception.getMessage());
            }else{
                throw new RuntimeException(exception);
            }
        }
    }

    @Exposed
    public boolean isDir(String path){
        Filepath file = getFile(path);
        return file.isDirectory();
    }

    @Exposed
    public boolean isFile(String path){
        Filepath file = getFile(path);
        return file.isDirectory();
    }

    @Exposed
    public List getChildren(String path){
        Filepath file = getFile(path);
        if (file.isInvalid()) throw new ExposedError("Invalid file path");
        if (!file.exists()) throw new ExposedError("File does not exist");
        if (!file.isDirectory()) throw new ExposedError("Not a directory");
        try{
            Filepath[] files = file.listFiles();
            String[] paths = new String[files.length];
            for (int i = 0; i < files.length; i++){
                paths[i] = files[i].getName();
            }
            Arrays.sort(paths);
            return new List((Object[]) paths);
        }catch (Exception exception){
            if (exception instanceof IOException){
                throw new ExposedError(exception.getMessage());
            }else{
                throw new RuntimeException(exception);
            }
        }
    }

    @Exposed
    public boolean delete(String path){
        if (FileHelper.normalize(path).equals(FileHelper.normalize(build.entrypoint))) throw new ExposedError("Access denied");
        Filepath file = getFile(path);
        if (file.isInvalid()) throw new ExposedError("Invalid file path");
        if (!file.exists()) throw new ExposedError("File does not exist");
        if (!file.canWrite()) throw new ExposedError("Access denied");
        try{
            return file.delete();
        }catch (Exception exception){
            if (exception instanceof IOException){
                throw new ExposedError(exception.getMessage());
            }else{
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    public NbtCompound saveAsNBT() {
        return build.save();
    }

    public static FileSpace fetchFromNBT(NbtCompound data, Computer computer) {
        return null;
    }

    public Table LangizeFile(Filepath filepath, OpeningMode mode){
        return APILoader.TableizeAPI(new FileHeader(filepath, this, mode), parent.getRuntime());
    }

    @Override
    public @NotNull BootPath fetchBootPath() {
        return new BootPath(getFile(FileHelper.normalize(build.entrypoint)), NeetComputers.getLanguage("Lua 5.2"));
    }

    @Override
    public boolean canBoot() throws IOException{
        Filepath file = getFile(FileHelper.normalize(build.entrypoint));
        if (!file.exists()) throw new IOException("File does not exist");
        if (!file.isFile()) throw new IOException("File cant be a directory");
        if (!file.canRead()) throw new IOException("File not readable");
        return true;
    }

    @Override
    public boolean kill(PeripheralConsumer peripheralConsumer) {
        return parent.isDead();
    }

    @Override
    public Exposable getAPI(PeripheralConsumer peripheralConsumer) {
        return this;
    }

    @Override
    public void tick(PeripheralConsumer peripheralConsumer, short deltaTime) {

    }

    @Override
    public boolean impermeable() {
        return true;
    }

    @Override
    public UUID getProviderUuid() {
        return parent.getUuid();
    }

    @Override
    public String getType() {
        return "File System";
    }
}