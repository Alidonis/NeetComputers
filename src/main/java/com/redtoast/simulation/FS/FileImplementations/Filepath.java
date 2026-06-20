package com.redtoast.simulation.FS.FileImplementations;

import java.io.IOException;

public interface Filepath {
    boolean isInvalid();
    String getName();
    boolean canRead();
    boolean canWrite();
    boolean exists();
    boolean isDirectory();
    boolean isFile();
    boolean createNewFile() throws IOException;
    String readAll() throws IOException;
    boolean write(byte[] bytes) throws IOException;
    boolean append(byte[] bytes) throws IOException;
    byte[] readAllBinary() throws IOException;
    boolean writeBinary(byte[] bytes) throws IOException;
    boolean appendBinary(byte[] bytes) throws IOException;
    boolean delete() throws IOException;
    String[] listFiles() throws IOException;
    boolean mkdirs()  throws IOException;
    default boolean mkdir() throws IOException {return mkdirs();}
    String getPath();
}
