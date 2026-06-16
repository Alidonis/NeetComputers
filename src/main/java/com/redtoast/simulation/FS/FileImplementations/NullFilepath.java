package com.redtoast.simulation.FS.FileImplementations;

import com.redtoast.simulation.FS.FileHelper;

import java.io.IOException;

public class NullFilepath implements Filepath {
    private String path;
    public NullFilepath(String Path){
        path = FileHelper.normalize(Path);
    }
    @Override
    public boolean isInvalid() {
        return true;
    }

    @Override
    public String getName() {
        String[] parts = path.split("\\\\");
        return parts[parts.length-1];
    }

    @Override
    public boolean isAbsolute() {
        return FileHelper.isAbsolute(path);
    }

    @Override
    public boolean canRead() {
        return false;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean createNewFile() throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public String readAll() throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public boolean write(byte[] bytes) throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public boolean append(byte[] bytes) throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public byte[] readAllBinary() throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public boolean writeBinary(byte[] bytes) throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public boolean appendBinary(byte[] bytes) throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public boolean delete() throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public Filepath[] listFiles() throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public boolean mkdirs() throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public boolean renameTo(Filepath dest) throws IOException {
        throw new IOException("Invalid file path");
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof Filepath path){
            return getPath().equals(path.getPath());
        }
        return super.equals(obj);
    }
}
