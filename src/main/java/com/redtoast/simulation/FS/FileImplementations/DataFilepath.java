package com.redtoast.simulation.FS.FileImplementations;

import com.redtoast.neet.NeetComputers;
import com.redtoast.simulation.FS.Filepath;
import com.redtoast.simulation.FS.FileHelper;
import com.redtoast.simulation.FS.FileSystem;
import com.redtoast.simulation.FS.Partition;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

public class DataFilepath implements Filepath {
    private final int pointer;
    private final String path;
    private String relPath;
    private final FileSystem fs;
    private boolean invalid = false;

    public DataFilepath(int Pointer, String Path, FileSystem system){
        pointer = Pointer;
        path = FileHelper.normalize(Path);
        relPath = FileHelper.deAbsulutize(path).replace('\\','/');
        if (relPath.endsWith("/")){
            relPath = relPath.substring(0,relPath.length()-1);
        }
        invalid = !FileHelper.validatePathStatic(path);
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
        return FileHelper.isAbsulute(path);
    }

    @Override
    public boolean canRead() {
        return isFile();
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean exists() {
        if (isInvalid()) return false;
        if (fs.build.blacklist.contains(getPath())) return false;
        return NeetComputers.datahandling.getResource(new Identifier("neetcomputers","hard_addresses/"+-pointer+relPath)).isPresent();
    }

    @Override
    public boolean isDirectory() {
        if (!exists()) return false;
        String Spath = "hard_addresses/"+-pointer+relPath;
        if (!Spath.endsWith("/")){
            Spath += "/";
        }
        try {
            NeetComputers.datahandling.getResource(new Identifier("neetcomputers",Spath)).get().getReader();
            return false;
        } catch (IOException e) {
            if (!(e instanceof java.nio.file.AccessDeniedException)){
                throw new RuntimeException(e);
            }
            return true;
        }
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
        return partition.hidden();
    }

    @Override
    public boolean createNewFile() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        return false;
    }

    @Override
    public String readAll() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isFile()) return null;
        if (exists()){
            BufferedReader reader = NeetComputers.datahandling.getResource(new Identifier("neetcomputers","hard_addresses/"+-pointer+relPath)).get().getReader();
            StringBuilder buffer = new StringBuilder();
            while (reader.ready()){
                buffer.append('\n');
                buffer.append(reader.readLine());
            }
            return buffer.substring(1);
        }else{
            return null;
        }
    }

    @Override
    public boolean write(byte[] bytes) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        return false;
    }

    @Override
    public boolean append(byte[] bytes) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        return false;
    }

    @Override
    public boolean delete() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        return false;
    }

    @Override
    public Filepath[] listFiles() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        if (!isDirectory()) return null;
        String spath;
        if (("hard_addresses/"+-pointer+relPath).endsWith("/")){
            spath = "hard_addresses/"+-pointer+relPath;
        }else{
            spath = "hard_addresses/"+-pointer+relPath+"/";
        }
        String jpath = getPath();
        String[] parts = jpath.split("\\\\");
        jpath = "";
        for (String part : parts){
            jpath += part + '\\';
        }
        LinkedList<String> filepaths = new LinkedList<>();
        String finalJpath = jpath;
        NeetComputers.datahandling.findResources("hard_addresses/"+-pointer+relPath, arg -> {
            String gpath = arg.toString().split(spath)[1];
            String name = gpath.split("/")[0];
            if (!filepaths.contains(finalJpath +name) && !fs.build.blacklist.contains(finalJpath +name)){
                filepaths.add(finalJpath +name);
            }
            return true;
        });
        Filepath[] files = new Filepath[filepaths.size()];
        for (int i = 0; i < files.length; i++){
            files[i] = fs.getFile(filepaths.get(i));
        }
        return files;
    }

    @Override
    public boolean mkdirs() throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        return false;
    }

    @Override
    public boolean renameTo(Filepath dest) throws IOException {
        if (invalid) throw new IOException("Invalid file path");
        return false;
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
