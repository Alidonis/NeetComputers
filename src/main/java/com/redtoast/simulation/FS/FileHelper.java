package com.redtoast.simulation.FS;

import com.redtoast.simulation.FS.FileImplementations.DataFilepath;
import com.redtoast.simulation.FS.FileImplementations.DualFilepath;
import com.redtoast.simulation.FS.FileImplementations.NullFilepath;
import com.redtoast.simulation.FS.FileImplementations.RealFilepath;

public class FileHelper {
    public static Filepath getFile(FileSystem fs, String path){
        String normalizedPath = normalize(path);
        if (validatePathStatic(normalizedPath)){
            String[] components = normalizedPath.split(":");
            if (fs.partitionExists(components[0])){
                if (isSourceHardAddress(fs.getPartition(components[0]).source())){
                    Partition partition = fs.getPartition(components[0]);
                    DataFilepath dataFilepath = new DataFilepath(Integer.valueOf(partition.source()), normalizedPath, fs);
                    if (partition.readOnly()){
                        return dataFilepath;
                    }else{
                        return new DualFilepath(dataFilepath, new RealFilepath(fs.basePath.resolve(components[0]), normalizedPath, fs), fs);
                    }
                }else{
                    return new RealFilepath(fs.basePath.resolve(components[0]), normalizedPath, fs);
                }
            }else{
                return new NullFilepath(normalizedPath);
            }
        }else{
            return new NullFilepath(normalizedPath);
        }
    }

    public static String normalize(String path){
        String[] secs = path.split(":");
        if (secs.length==0){
            return ":\\";
        }
        if (secs.length==1){
            return path+"\\";
        }
        StringBuilder buffer = new StringBuilder(secs[secs.length-1]);
        buffer.insert(0, '\\');
        while (buffer.indexOf("/")!=-1){
            buffer.replace(buffer.indexOf("/"), buffer.indexOf("/")+1, "\\");
        }
        while (buffer.indexOf("\\\\")!=-1){
            buffer.replace(buffer.indexOf("\\\\"), buffer.indexOf("\\\\")+2, "\\");
        }
        if (secs.length==1){
            buffer.insert(0,':');
            return buffer.toString();
        }else{
            for (int i = 0; i < secs.length-1; i++){
                buffer.insert(0,':');
                buffer.insert(0, secs[i]);
            }
            return buffer.toString();
        }
    }

    public static String deAbsulutize(String path){
        String[] parts = path.split(":");
        return parts[parts.length-1];
    }

    public static boolean isAbsulute(String path){
        String[] parts = path.split(":");
        if (parts.length<2){
            return false;
        }
        return !parts[0].isBlank();
    }

    public static boolean validatePathStatic(String path){
        String[] parts = path.split(":\\\\");
        if (parts.length>2) return false;
        char[] chars = parts[0].toCharArray();
        if (chars.length==0){
            return false;
        }
        for (int x = 0; x < chars.length; x++){
            char letter = chars[x];
            if (!((int)letter>=(int)'a' && (int)letter<=(int)'z') || ((int)letter>=(int)'A' && (int)letter<=(int)'Z')){
                return false;
            }
        }
        parts = deAbsulutize(parts[parts.length-1]).split("\\\\");
        for (int i = 0; i < parts.length; i++){
            chars = parts[i].toCharArray();
            if (chars.length==0 && i<parts.length-1){
                return false;
            }
            for (int x = 0; x < chars.length; x++){
                char letter = chars[x];
                if (!((int)letter>=(int)'a' && (int)letter<=(int)'z') && !((int)letter>=(int)'A' && (int)letter<=(int)'Z') && !((int)letter>=(int)'0' && (int)letter<=(int)'9')){
                    if (!(letter=='-' || letter=='_' || (letter==' ' && x< chars.length-1) || (i==parts.length-1 && letter=='.' && x!=0))){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isSourceHardAddress(String source){
        if (source==null) return false;
        if (source.isBlank()) return false;
        return source.charAt(0) == '-';
    }

    public static openMode getMode(String mode){
        if (mode.length()>2) return openMode.INVALID;
        if (mode.isBlank()) return openMode.INVALID;
        openMode enumm = switch (mode.toLowerCase().charAt(0)){
            case 'r' -> openMode.READ;
            case 'w' -> openMode.WRITE;
            case 'a' -> openMode.APPEND;
            default -> openMode.INVALID;
        };
        if (mode.length()==1){
            return enumm;
        }else if (mode.charAt(1)=='+'){
            return switch (enumm){
                case READ, APPEND -> openMode.APPENDPLUS;
                case WRITE -> openMode.WRITEPLUS;
                default -> openMode.INVALID;
            };
        }else{
            return openMode.INVALID;
        }
    }
}
