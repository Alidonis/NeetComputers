package com.redtoast.simulation.FS.FileImplementations;

import com.redtoast.simulation.FS.FileSystem;
import com.redtoast.simulation.FS.Filepath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class DualFilepath implements Filepath {
    private RealFilepath realFilepath;
    private DataFilepath dataFilepath;
    private FileSystem fs;
    public DualFilepath(DataFilepath dataFilepath, RealFilepath realFilepath, FileSystem fs) {
        this.dataFilepath=dataFilepath;
        this.realFilepath=realFilepath;
        this.fs = fs;
        if (realFilepath.exists()) fs.build.blacklist.remove(getPath());
    }

    @Override
    public boolean isInvalid() {
        return dataFilepath.isInvalid();
    }

    @Override
    public String getName() {
        return dataFilepath.getName();
    }

    @Override
    public boolean isAbsolute() {
        return dataFilepath.isAbsolute();
    }

    @Override
    public boolean canRead() {
        return realFilepath.exists() ? realFilepath.canRead() : dataFilepath.canRead();
    }

    @Override
    public boolean canWrite() {
        return realFilepath.canWrite();
    }

    @Override
    public boolean exists() {
        if (isInvalid()) return false;
        if (fs.build.blacklist.contains(getPath())) return false;
        return dataFilepath.exists() || realFilepath.exists();
    }

    @Override
    public boolean isDirectory() {
        return realFilepath.exists() ? realFilepath.isDirectory() : dataFilepath.isDirectory();
    }

    @Override
    public boolean isFile() {
        return realFilepath.exists() ? realFilepath.isFile() : dataFilepath.isFile();
    }

    @Override
    public boolean isHidden() {
        return dataFilepath.isHidden();
    }

    @Override
    public boolean createNewFile() throws IOException {
        if (exists()) return false;
        fs.build.blacklist.remove(getPath());
        return realFilepath.createNewFile();
    }

    @Override
    public String readAll() throws IOException {
        return realFilepath.exists() ? realFilepath.readAll() : dataFilepath.readAll();
    }

    @Override
    public boolean write(byte[] bytes) throws IOException {
        if (!realFilepath.exists()) realFilepath.createNewFile();
        return realFilepath.write(bytes);
    }

    @Override
    public boolean append(byte[] bytes) throws IOException {
        if (isInvalid()) throw new IOException("Invalid file path");
        if (realFilepath.exists()){
            return realFilepath.append(bytes);
        }else{
            boolean result = realFilepath.write(dataFilepath.readAll().getBytes(StandardCharsets.UTF_8));
            if (result){
                return realFilepath.append(bytes);
            }else{
                return false;
            }
        }
    }

    @Override
    public boolean delete() throws IOException {
        if (isInvalid()) throw new IOException("Invalid file path");
        if (fs.build.blacklist.contains(getPath())) return false;
        if (realFilepath.exists()) {
            boolean check = realFilepath.delete();
            if (!check) return false;
        }
        fs.build.blacklist.add(getPath());
        return true;
    }

    @Override
    public Filepath[] listFiles() throws IOException {
        if (!realFilepath.exists()) return dataFilepath.listFiles();
        LinkedList<Filepath> files = new LinkedList<>(Arrays.asList(dataFilepath.listFiles()));
        for (Filepath file : realFilepath.listFiles()){
            if (!files.contains(file) && !fs.build.blacklist.contains(file.getPath())) files.add(file);
        }
        return files.toArray(new Filepath[]{});
    }

    @Override
    public boolean mkdirs() throws IOException {
        return realFilepath.mkdirs();
    }

    @Override
    public boolean renameTo(Filepath dest) throws IOException {
        if (isInvalid()) throw new IOException("Invalid file path");
        throw new IOException("Dual File paths (write mode preset partitions) don't support .rename(String dest)");
    }

    @Override
    public String getPath() {
        return dataFilepath.getPath();
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof Filepath path){
            return getPath().equals(path.getPath());
        }
        return super.equals(obj);
    }
}
