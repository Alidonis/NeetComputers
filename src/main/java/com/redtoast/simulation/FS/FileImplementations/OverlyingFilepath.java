package com.redtoast.simulation.FS.FileImplementations;

import com.redtoast.simulation.FS.FileSystem;
import com.redtoast.simulation.FS.Filepath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class OverlyingFilepath implements Filepath {
    private Filepath mainFilepath;
    private Filepath layoverFilepath;
    private FileSystem fs;
    public OverlyingFilepath(Filepath mainFilepath, Filepath layoverFilepath, FileSystem fs) {
        this.layoverFilepath = layoverFilepath;
        this.mainFilepath = mainFilepath;
        this.fs = fs;
        if (mainFilepath.exists()) fs.build.blacklist.remove(getPath());
    }

    @Override
    public boolean isInvalid() {
        return layoverFilepath.isInvalid();
    }

    @Override
    public String getName() {
        return layoverFilepath.getName();
    }

    @Override
    public boolean isAbsolute() {
        return layoverFilepath.isAbsolute();
    }

    @Override
    public boolean canRead() {
        return mainFilepath.exists() ? mainFilepath.canRead() : layoverFilepath.canRead();
    }

    @Override
    public boolean canWrite() {
        return mainFilepath.canWrite();
    }

    @Override
    public boolean exists() {
        if (isInvalid()) return false;
        if (fs.build.blacklist.contains(getPath())) return false;
        return layoverFilepath.exists() || mainFilepath.exists();
    }

    @Override
    public boolean isDirectory() {
        return mainFilepath.exists() ? mainFilepath.isDirectory() : layoverFilepath.isDirectory();
    }

    @Override
    public boolean isFile() {
        return mainFilepath.exists() ? mainFilepath.isFile() : layoverFilepath.isFile();
    }

    @Override
    public boolean isHidden() {
        return layoverFilepath.isHidden();
    }

    @Override
    public boolean createNewFile() throws IOException {
        if (exists()) return false;
        fs.build.blacklist.remove(getPath());
        return mainFilepath.createNewFile();
    }

    @Override
    public String readAll() throws IOException {
        return mainFilepath.exists() ? mainFilepath.readAll() : layoverFilepath.readAll();
    }

    @Override
    public boolean write(byte[] bytes) throws IOException {
        if (!mainFilepath.exists()) mainFilepath.createNewFile();
        return mainFilepath.write(bytes);
    }

    @Override
    public boolean append(byte[] bytes) throws IOException {
        if (isInvalid()) throw new IOException("Invalid file path");
        if (mainFilepath.exists()){
            return mainFilepath.append(bytes);
        }else{
            boolean result = mainFilepath.writeBinary(layoverFilepath.readAllBinary());
            if (result){
                return mainFilepath.append(bytes);
            }else{
                return false;
            }
        }
    }

    @Override
    public byte[] readAllBinary() throws IOException {
        return mainFilepath.exists() ? mainFilepath.readAllBinary() : layoverFilepath.readAllBinary();
    }

    @Override
    public boolean writeBinary(byte[] bytes) throws IOException {
        if (!mainFilepath.exists()) mainFilepath.createNewFile();
        return mainFilepath.writeBinary(bytes);
    }

    @Override
    public boolean appendBinary(byte[] bytes) throws IOException {
        if (isInvalid()) throw new IOException("Invalid file path");
        if (mainFilepath.exists()){
            return mainFilepath.appendBinary(bytes);
        }else{
            boolean result = mainFilepath.writeBinary(layoverFilepath.readAllBinary());
            if (result){
                return mainFilepath.appendBinary(bytes);
            }else{
                return false;
            }
        }
    }

    @Override
    public boolean delete() throws IOException {
        if (isInvalid()) throw new IOException("Invalid file path");
        if (fs.build.blacklist.contains(getPath())) return false;
        if (mainFilepath.exists()) {
            boolean check = mainFilepath.delete();
            if (!check) return false;
        }
        fs.build.blacklist.add(getPath());
        return true;
    }

    @Override
    public Filepath[] listFiles() throws IOException {
        if (!mainFilepath.exists()) return layoverFilepath.listFiles();
        LinkedList<Filepath> files = new LinkedList<>(Arrays.asList(layoverFilepath.listFiles()));
        for (Filepath file : mainFilepath.listFiles()){
            if (!files.contains(file) && !fs.build.blacklist.contains(file.getPath())) files.add(file);
        }
        return files.toArray(new Filepath[]{});
    }

    @Override
    public boolean mkdirs() throws IOException {
        return mainFilepath.mkdirs();
    }

    @Override
    public boolean renameTo(Filepath dest) throws IOException {
        if (isInvalid()) throw new IOException("Invalid file path");
        throw new IOException("Dual File paths (write mode preset partitions) don't support .rename(String dest)");
    }

    @Override
    public String getPath() {
        return layoverFilepath.getPath();
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof Filepath path){
            return getPath().equals(path.getPath());
        }
        return super.equals(obj);
    }
}
