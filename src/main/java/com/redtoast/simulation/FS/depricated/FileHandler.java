package com.redtoast.simulation.FS.depricated;

import com.redtoast.neet.NeetComputers;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Path;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.nio.file.Files;

@Deprecated
public class FileHandler {
    private static final Logger debug = LoggerFactory.getLogger("NeetComputers:debug-file_system");
    private LinkedList<rootDir> roots = new LinkedList<>();

    public class rootDir{
        public int pointer;
        public String path;
        public String mountname;
        public boolean readOnly;
        public Path pathClass;
        public int capacity;
        public int bytes;
        public rootDir ChildOf;
        public rootDir(int p, String pth, String m, boolean ro){
            pointer=p;
            path=pth;
            mountname=m;
            readOnly=ro;
            if (pointer<0){
                readOnly=true;
            }else{
                pathClass = NeetComputers.worldPath.resolve("neetcomputers").resolve(String.valueOf(pointer));
                pathClass.toFile().mkdir();
                pathClass = pathClass.resolve(pth);
                pathClass.toFile().mkdir();
                pathClass = pathClass.normalize();
            }
        }
    }

    public FileHandler(int pointer, int rom, String os){
        mountDrive(pointer,"user","user");
        mountDrive(rom,"rom",os,true);
    }

    public rootDir findRoot(String path){
        int start;
        if (path.charAt(0)=='/' || path.charAt(0)=='\\'){
            start=1;
        }else{
            start=0;
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = start; i < path.length(); i++){
            if (path.charAt(i)=='/' || path.charAt(i)=='\\'){
                i = path.length();
            }else if(path.charAt(i)!=' '){
                buffer.append(path.charAt(i));
            }
        }
        String rootname = buffer.toString();
        rootDir root = null;
        for (int i = 0; i < roots.size(); i++){
            if (Objects.equals(roots.get(i).path, rootname)){
                root = roots.get(i);
            }
        }
        return root;
    }

    public static String deObjectivify(String path){
        int start;
        if (path.charAt(0)=='/' || path.charAt(0)=='\\'){
            start=1;
        }else{
            start=0;
        }
        boolean collect = false;
        StringBuilder buffer = new StringBuilder();
        for (int i = start; i < path.length(); i++){
            if (path.charAt(i)=='\\' || path.charAt(i)=='/'){
                collect = true;
            }
            if (collect){
                buffer.append(path.charAt(i));
            }
        }
        return buffer.toString();
    }

    public boolean rootExists(String path){
        rootDir root = findRoot(path);
        return root!=null;
    }

    public void makeDir(String path){
        rootDir root = findRoot(path);
        String npath = deObjectivify(path).replace('\\', '/');
        if (!npath.equals("")){
            npath = npath.substring(1);
        }
        String[] parts = getParts(npath);
        Path resolve = root.pathClass;
        for (int i = 0; i < parts.length; i++){
            resolve =resolve.resolve(parts[i]);
            resolve.toFile().mkdir();
        }
    }

    public String[] getRoots(){
        String[] rootpaths = new String[roots.size()];
        for (int i = 0; i < roots.size(); i++){
            rootpaths[i] = roots.get(i).path;
        }
        return rootpaths;
    }

    public String[] getFiles(String path){
        if (path.equals("")){
            return getRoots();
        }
        if (path.length()==1){
            if (path.charAt(0)=='/' || path.charAt(0)=='\\'){
                return getRoots();
            }
        }
        rootDir root = findRoot(path);
        String npath = deObjectivify(path).replace('\\', '/');
        if (!npath.equals("")){
            npath = npath.substring(1);
        }
        if (npath.length()==0){
            npath = "/";
        }else {
            if (npath.charAt(npath.length() - 1) != '/') {
                npath = npath + '/';
            }
        }
        if (npath.equals("/")){
            if (root.pointer<0){
                String spath = "hard_addresses/"+-root.pointer;
                Identifier directory = new Identifier("neetcomputers", spath);
                Map<Identifier, Resource> resources = NeetComputers.datahandling.findResources(spath, arg -> true);
                String[] filenames = new String[resources.size()];
                int i = 0;
                for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                    Identifier id = entry.getKey();
                    String filepathRelitive = id.toString().substring(spath.length()+10);
                    if (!filepathRelitive.contains("/")){
                        filenames[i] = filepathRelitive;
                        i++;
                    }
                }
                String[] filesnames2 = new String[i];
                for (int x = 0; x < i; x++){
                    filesnames2[x] = filenames[x];
                }
                return filesnames2;
            }else{
                File[] files = root.pathClass.toFile().listFiles();
                String[] filenames = new String[files.length];
                for (int i = 0; i < files.length; i++){
                    filenames[i] = files[i].getName();
                }
                return filenames;
            }
        }else{
            if (root.pointer<0){
                Identifier directory = new Identifier("neetcomputers", "hard_addresses/"+-root.pointer+"/"+npath.substring(0,npath.length()-1));
                Map<Identifier, Resource> resources = NeetComputers.datahandling.findResources("hard_addresses/"+-root.pointer+"/"+npath.substring(0,npath.length()-1), arg -> true);
                String[] filenames = new String[resources.size()];
                int i = 0;
                for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                    Identifier id = entry.getKey();
                    if (!id.toString().contains("/")){
                        filenames[i] = id.toString().split(":")[1];
                        i++;
                    }
                }
                String[] filesnames2 = new String[i];
                for (int x = 0; x < i; x++){
                    filesnames2[x] = filenames[x];
                }
                return filesnames2;
            }else{
                File[] files = root.pathClass.resolve(npath).toFile().listFiles();
                String[] filenames = new String[files.length];
                for (int i = 0; i < files.length; i++){
                    filenames[i] = files[i].getName();
                }
                return filenames;
            }
        }
    }

    public boolean exists(String path){
        if (path.length()<=1){
            if (path.isEmpty()){
                return true;
            }else{
                return path.charAt(0) == '/' || path.charAt(0) == '\\';
            }
        }
        String npath;
        if (path.charAt(0)=='\\' || path.charAt(0)=='/'){
            npath = path;
        }else{
            npath = "/" + path;
        }
        rootDir root;
        root = findRoot(path);
        if (root==null){
            return false;
        }
        npath = deObjectivify(npath).replace('\\','/');
        if (!npath.equals("")){
            npath = npath.substring(1);
        }
        if (!validatePath("dummy/"+npath)) {
            return false;
        }
        if (root.pointer<0){
            return NeetComputers.datahandling.getResource(new Identifier("neetcomputers","hard_addresses/"+-root.pointer+"/"+npath)).isPresent();
        }else{
            Path spath = root.pathClass.resolve(npath);
            return spath.toFile().exists();
        }
    }

    public String readFile(String path){
        rootDir root = findRoot(path);
        String npath = deObjectivify(path).replace('\\', '/');
        if (!npath.equals("")){
            npath = npath.substring(1);
        }
        if (root.pointer<0){
            try{
                BufferedReader reader = NeetComputers.datahandling.getResource(new Identifier("neetcomputers","hard_addresses/"+-root.pointer+"/"+npath)).get().getReader();
                StringBuilder buffer = new StringBuilder();
                while (reader.ready()){
                    buffer.append('\n');
                    buffer.append(reader.readLine());
                }
                return buffer.substring(1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            Path spath = root.pathClass.resolve(npath);
            try{
                return Files.readString(spath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isDir(String path){
        if (path.length()<=1){
            if (path.isEmpty()){
                return true;
            }else{
                return path.charAt(0) == '/' || path.charAt(0) == '\\';
            }
        }
        rootDir root = findRoot(path);
        String npath = deObjectivify(path).replace('\\', '/');
        if ((npath.equals("") || npath.equals("/")) && root!=null){
            return true;
        }
        npath = npath.substring(1);
        if (root.pointer<0){
            String Spath = "hard_addresses/"+-root.pointer+"/"+npath;
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
        }else{
            Path spath = root.pathClass.resolve(npath);
            return Files.isDirectory(spath);
        }
    }

    public void mountDrive(int pointer, String path, String name, boolean readOnly){
        roots.add(new rootDir(pointer,path,name,readOnly));
    }
    public void mountDrive(int pointer, String path, String name){
        roots.add(new rootDir(pointer,path,name,false));
    }
    public void mountDrive(int pointer, String path, boolean readOnly){
        roots.add(new rootDir(pointer,path,"null",readOnly));
    }
    public void mountDrive(int pointer, String path){
        roots.add(new rootDir(pointer,path,"null",false));
    }

    public static String[] getParts(String path){
        int start;
        if (path.charAt(0)=='/' || path.charAt(0)=='\\'){
            start=1;
        }else{
            start=0;
        }
        LinkedList<String> parts = new LinkedList<>();
        StringBuilder part = new StringBuilder();
        for (int i = start; i < path.length(); i++){
            char letter = path.charAt(i);
            if (letter=='/' || letter=='\\'){
                parts.add(part.toString());
                part.delete(0,part.length());
            }else{
                part.append(letter);
            }
        }
        parts.add(part.toString());
        String[] partsArray = new String[parts.size()];
        for (int i = 0; i < parts.size(); i++){
            partsArray[i] = parts.get(i);
        }
        return partsArray;
    }

    public boolean validatePath(String path){
        String[] parts = getParts(path);
        for (int i = 0; i < parts.length; i++){
            char[] chars = parts[i].toCharArray();
            if (chars.length==0 && i<parts.length-1){
                return false;
            }
            for (int x = 0; x < chars.length; x++){
                char letter = chars[x];
                if (!((int)letter>=(int)'a' && (int)letter<=(int)'z') || ((int)letter>=(int)'A' && (int)letter<=(int)'Z') || ((int)letter>=(int)'0' && (int)letter<=(int)'9')){
                    if (!(letter=='-' || letter=='_' || (letter==' ' && x< chars.length-1) || (i==parts.length-1 && letter=='.' && i!=0))){
                        return false;
                    }
                }
            }
        }
        rootDir root = findRoot(path);
        if (root!=null){
            if (root.pointer>=0){
                String npath = deObjectivify(path).replace('\\', '/');
                if (!npath.equals("")){
                    npath = npath.substring(1);
                }
                npath = deObjectivify(npath).replace('\\','/');
                Path spath = root.pathClass.resolve(npath).normalize();
                try {
                    if (!spath.startsWith(root.pathClass)) {
                        return false;
                    }
                    if (!spath.toFile().exists()){
                        return true;
                    }
                    if (!spath.toRealPath().startsWith(root.pathClass.toRealPath())){
                        return false;
                    }
                }catch (Exception e){
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean validatePathStatic(String path){
        String[] parts = getParts(path);
        for (int i = 0; i < parts.length; i++){
            char[] chars = parts[i].toCharArray();
            if (chars.length==0 && i<parts.length-1){
                return false;
            }
            for (int x = 0; x < chars.length; x++){
                char letter = chars[x];
                if (!((int)letter>=(int)'a' && (int)letter<=(int)'z') || ((int)letter>=(int)'A' && (int)letter<=(int)'Z') || ((int)letter>=(int)'0' && (int)letter<=(int)'9')){
                    if (!(letter=='-' || letter=='_' || (letter==' ' && x< chars.length-1) || (i==parts.length-1 && letter=='.' && i!=0))){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
