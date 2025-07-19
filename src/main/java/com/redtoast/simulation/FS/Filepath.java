package com.redtoast.simulation.FS;

import java.io.IOException;

public interface Filepath {
    boolean isInvalid();
    String getName();
    boolean isAbsolute();
    boolean canRead();
    boolean canWrite();
    boolean exists();
    boolean isDirectory();
    boolean isFile();
    boolean isHidden();
    boolean createNewFile() throws IOException;
    String readAll() throws IOException;
    boolean write(byte[] bytes) throws IOException;
    boolean append(byte[] bytes) throws IOException;
    byte[] readAllBinary() throws IOException;
    boolean writeBinary(byte[] bytes) throws IOException;
    boolean appendBinary(byte[] bytes) throws IOException;
    boolean delete() throws IOException;
    Filepath[] listFiles() throws IOException;
    boolean mkdirs()  throws IOException;
    default boolean mkdir() throws IOException {return mkdirs();}
    boolean renameTo(Filepath dest)  throws IOException;
    String getPath();
}
