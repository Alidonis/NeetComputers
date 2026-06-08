package com.redtoast.simulation.FS.FileImplementations;

import com.redtoast.simulation.FS.FileHelper;
import com.redtoast.simulation.FS.DiskSystem;
import com.redtoast.simulation.FS.Partition;
import org.apache.commons.io.FileUtils;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RealFilepath implements Filepath {
    private final Path root;
    private final String path;
    private final String relPath;
    private final DiskSystem fs;
    private boolean invalid = false;

    public RealFilepath(Path Root, String Path, DiskSystem system){
        root = Root;
        path = FileHelper.normalize(Path);
        relPath = FileHelper.deAbsolutize(path).replace('\\','/').substring(1);
        invalid = !FileHelper.validatePathStatic(path);
        if (!invalid){
            Path spath = root.resolve(relPath).normalize();
            try {
                if (!spath.startsWith(root)) {
                    invalid = false;
                }
                if (!spath.toFile().exists()){
                    invalid = false;
                }
                if (!spath.toRealPath().startsWith(root.toRealPath())){
                    invalid = false;
                }
            }catch (Exception e){
                invalid = false;
            }
        }
        fs = system;
    }
    @Override
    public boolean isInvalid() {
        return invalid;
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
        return isFile();
    }

    @Override
    public boolean canWrite() {
        if (invalid) return false;
        String[] components = path.split(":");
        Partition partition = fs.getPartition(components[0]);
        if (partition==null) return false;
        return !partition.readOnly();
    }

    @Override
    public boolean exists() {
        if (invalid) return false;
        Path spath = root.resolve(relPath);
        return spath.toFile().exists();
    }

    @Override
    public boolean isDirectory() {
        if (!exists()) return false;
        Path spath = root.resolve(relPath);
        return Files.isDirectory(spath);
    }

    @Override
    public boolean isFile() {
        if (!exists()) return false;
        return !isDirectory();
    }

    @Override
    public boolean isHidden() {
        if (invalid) return false;
        String[] components = path.split(":");
        Partition partition = fs.getPartition(components[0]);
        if (partition==null) return false;
        if (partition.hidden()) return true;
        Path spath = root.resolve(relPath);
        return spath.toFile().isHidden();
    }

    @Override
    public boolean createNewFile() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!exists()){
            mkdirs(1);
            Path spath = root.resolve(relPath);
            return spath.toFile().createNewFile();
        }
        return false;
    }

    @Override
    public String readAll() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isFile()) return null;
        Path spath = root.resolve(relPath);
        try{
            return Files.readString(spath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean write(byte[] bytes) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isFile()) return false;
        if (!canWrite()) return false;
        Path spath = root.resolve(relPath);
        FileWriter writer = new FileWriter(spath.normalize().toFile(), false);
        writer.write(new String(bytes, StandardCharsets.UTF_8));
        writer.close();
        return true;
    }

    @Override
    public boolean append(byte[] bytes) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isFile()) return false;
        if (!canWrite()) return false;
        Path spath = root.resolve(relPath);
        FileWriter writer = new FileWriter(spath.normalize().toFile(), true);
        writer.write(new String(bytes, StandardCharsets.UTF_8));
        writer.close();
        return true;
    }

    @Override
    public byte[] readAllBinary() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isFile()) return null;
        Path spath = root.resolve(relPath);
        try {
            return Files.readAllBytes(spath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean writeBinary(byte[] bytes) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isFile()) return false;
        if (!canWrite()) return false;
        Path spath = root.resolve(relPath);
        try (OutputStream out = new FileOutputStream(spath.toFile(), false)) {
            out.write(bytes);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean appendBinary(byte[] bytes) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isFile()) return false;
        if (!canWrite()) return false;
        Path spath = root.resolve(relPath);
        try (OutputStream out = new FileOutputStream(spath.toFile(), true)) {
            out.write(bytes);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!exists()) return false;
        if (!canWrite()) return false;
        Path spath = root.resolve(relPath);
        return FileUtils.deleteQuietly(spath.toFile());
    }

    @Override
    public Filepath[] listFiles() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isDirectory()) return null;
        java.io.File[] files = root.resolve(relPath).toFile().listFiles();
        if (files==null) return null;
        Filepath[] files2 = new Filepath[files.length];
        for (int i = 0; i < files.length; i++){
            String spath;
            if (path.endsWith("\\")){
                spath=path;
            }else{
                spath=path+'\\';
            }
            files2[i] = fs.getFile(spath+files[i].getName());
        }
        return files2;
    }

    private boolean mkdirs(int offset) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (exists()) return false;
        if (path.split("\\.").length>1 && offset==0) throw new IOException("Invalid file path");
        String[] parts = relPath.split("/");
        Path resolve = root;
        for (int i = 0; i < parts.length-offset; i++){
            resolve =resolve.resolve(parts[i]);
            resolve.toFile().mkdir();
        }
        return true;
    }

    @Override
    public boolean mkdirs() throws IOException {
        return mkdirs(0);
    }

    @Override
    public boolean renameTo(Filepath dest) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!exists()) return false;
        Path spath = root.resolve(relPath);
        return spath.toFile().renameTo(Path.of(dest.getPath()).toFile());
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
