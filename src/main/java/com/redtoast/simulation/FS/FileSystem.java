package com.redtoast.simulation.FS;

import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.APILoader;
import com.redtoast.simulation.FS.builder.FileContext;
import com.redtoast.simulation.FS.builder.SystemBuild;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.LangError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public class FileSystem implements API {
    public final SystemBuild build;
    public final int pointer;
    public final Path basePath;
    public final Hashtable<UUID, LangFile> map = new Hashtable<>();

    public FileSystem(SystemBuild build, int pointer) {
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
    }

    public FileSystem(SystemBuild build, int pointer, FileContext context){
        this(build, pointer);
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

    @Override
    public String getLabel() {
        return "file system";
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
    public boolean setReadOnly(String name){
        for (int i = 0; i < build.partitions.size(); i++){
            if (build.partitions.get(i).path().equals(name)){
                build.partitions.set(i, new Partition(name, true, build.partitions.get(i).hidden(), build.partitions.get(i).source()));
                return true;
            }
        }
        return false;
    }

    @Exposed(nameOverride = "open")
    public Table LangOpenFile(String path){
        return LangOpenFile(path, "r");
    }

    @Exposed(nameOverride = "open")
    public Table LangOpenFile(String path, String mode){
        OpeningMode openingMode = FileHelper.getMode(mode);
        Filepath filepath = getFile(path);
        if (filepath.isDirectory()) throw new LangError("Not a file");
        if (openingMode.invalid()) throw new LangError("Invalid open mode");
        if (!filepath.exists() && !openingMode.create()) throw new LangError("Not a file");
        if (openingMode.canRead() && !filepath.canRead()) throw new LangError("Access denied");
        if (openingMode.canWrite() && !filepath.canWrite()) throw new LangError("Access denied");
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
        if (!file.canWrite()) throw new LangError("Access denied");
        try{
            return file.mkdirs();
        }catch (Exception exception){
            if (exception instanceof IOException){
                throw new LangError(exception.getMessage());
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
        if (file.isInvalid()) throw new LangError("Invalid file path");
        if (!file.exists()) throw new LangError("File does not exist");
        if (!file.isDirectory()) throw new LangError("Not a directory");
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
                throw new LangError(exception.getMessage());
            }else{
                throw new RuntimeException(exception);
            }
        }
    }

    @Exposed
    public boolean delete(String path){
        if (FileHelper.normalize(path).equals(FileHelper.normalize(build.entrypoint))) throw new LangError("Access denied");
        Filepath file = getFile(path);
        if (file.isInvalid()) throw new LangError("Invalid file path");
        if (!file.exists()) throw new LangError("File does not exist");
        if (!file.canWrite()) throw new LangError("Access denied");
        try{
            return file.delete();
        }catch (Exception exception){
            if (exception instanceof IOException){
                throw new LangError(exception.getMessage());
            }else{
                throw new RuntimeException(exception);
            }
        }
    }

    public Table LangizeFile(Filepath filepath, OpeningMode mode){
        return APILoader.TableizeAPI(new LangFile(filepath, this, mode), null);
    }
}