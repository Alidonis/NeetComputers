package com.redtoast.simulation.FS;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.LangError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LangFile implements API {
    public final Filepath internal;
    public final openMode mode;
    public final FileSystem fs;
    public boolean canRead;
    public boolean canWrite;
    public boolean open = true;
    public int cursor = 0;
    public LinkedList<Byte> byteBuffer = new LinkedList<>();
    public LangFile(Filepath filepath, FileSystem fs, openMode mode){
        internal = filepath;
        this.mode = mode;
        this.fs = fs;
        try{
            switch (mode){
                case READ -> {
                    canRead=true;
                    canWrite=false;
                }
                case WRITE, APPEND -> {
                    canRead=false;
                    canWrite=true;
                }
                case WRITEPLUS, APPENDPLUS -> {
                    canRead=true;
                    canWrite=true;
                }
            }
            switch (mode){
                case READ, APPEND, APPENDPLUS -> {
                    if (filepath.exists()){
                        for (byte _byte : filepath.readAll().getBytes()){
                            byteBuffer.add(_byte);
                        }
                    }
                }
            }
            switch (mode){
                case WRITE, APPEND, WRITEPLUS -> {filepath.createNewFile();}
            }
        }catch (Throwable e){
            if (e instanceof IOException){
                throw new LangError(e.getMessage());
            }else{
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getLabel() {
        return "irreverent";
    }

    @Exposed
    public void flush(){
        if (!open) throw new LangError("Attempt to use a closed file");
        try{
            switch (mode){
                case WRITE, WRITEPLUS, APPEND, APPENDPLUS -> {
                    byte[] data = new byte[byteBuffer.size()];
                    for (int i = 0; i < byteBuffer.size(); i++){
                        data[i] = byteBuffer.get(i);
                    }
                    internal.write(data);
                }
            }
        }catch (Throwable e){
            if (e instanceof IOException){
                throw new LangError(e.getMessage());
            }else{
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Exposed
    public void close(){
        if (!open) throw new LangError("Attempt to use a closed file");
        flush();
        open = false;
        byteBuffer.clear();
    }

    @Exposed
    public int seek(String whence, int offset){
        if (whence.equals("set")){
            if (offset<0) throw new LangError("Invalid offset");
            if (offset>byteBuffer.size()) throw new LangError("Invalid offset");
            cursor = offset;
        }else if(whence.equals("cur")){
            int notcursor = cursor+offset;
            if (notcursor<0) throw new LangError("Invalid offset");
            if (notcursor>byteBuffer.size()) throw new LangError("Invalid offset");
            cursor = notcursor;
        }else if(whence.equals("end")){
            if (offset>0) throw new LangError("Invalid offset");
            if (offset>byteBuffer.size()) throw new LangError("Invalid offset");
            cursor = byteBuffer.size() + offset;
        }else{
            throw new LangError("Invalid option");
        }
        return cursor;
    }

    @Exposed
    public int seek(String whence){
        return seek(whence, 0);
    }

    @Exposed
    public int seek(){
        return cursor;
    }

    @Exposed
    public void write(int bytes){
        if (!open) throw new LangError("Attempt to use a closed file");
        if (!canWrite) throw new LangError("Access denied");
        byteBuffer.add((byte) bytes);
    }

    @Exposed
    public void write(String bytes){
        if (!open) throw new LangError("Attempt to use a closed file");
        if (!canWrite) throw new LangError("Access denied");
        for (byte _byte : bytes.getBytes()){
            byteBuffer.add(_byte);
        }
    }

    @Exposed
    public String read(){
        return read("l");
    }

    @Exposed
    public String read(String format){
        if (!open) throw new LangError("Attempt to use a closed file");
        if (!canRead) throw new LangError("Access denied");
        if (format.length()!=1)  throw new LangError("Invalid format");
        if (byteBuffer.size()==cursor) return "";
        return switch (format.charAt(0)){
            case 'l', 'L' -> {
                List<Byte> bytes = byteBuffer.subList(cursor, byteBuffer.size());
                byte[] data = new byte[bytes.size()];
                for (int i =0; i < bytes.size(); i++){
                    data[i] = bytes.get(i);
                }
                String text = new String(data, StandardCharsets.UTF_8);
                int endpoint = text.indexOf("\n");
                if (endpoint==-1){
                    cursor = byteBuffer.size();
                    yield text;
                }
                cursor = cursor + endpoint + 1;
                if (format.charAt(0)=='l'){
                    yield text.substring(0,endpoint);
                }else{
                    yield text.substring(0,endpoint) + '\n';
                }
            }
            case 'a' -> {
                List<Byte> bytes = byteBuffer.subList(cursor, byteBuffer.size());
                cursor = byteBuffer.size();
                byte[] data = new byte[bytes.size()];
                for (int i =0; i < bytes.size(); i++){
                    data[i] = bytes.get(i);
                }
                yield new String(data, StandardCharsets.UTF_8);
            }
            default -> throw new LangError("Invalid format");
        };
    }

    @Exposed
    public String read(int amount){
        if (!open) throw new LangError("Attempt to use a closed file");
        if (!canRead) throw new LangError("Access denied");
        int newcur = cursor+amount;
        if (newcur<0){
            newcur = 0;
        }
        if (newcur>byteBuffer.size()){
            newcur = byteBuffer.size();
        }
        if (newcur==cursor) return "";
        StringBuilder buffer = new StringBuilder();
        for (int i = Math.min(newcur, cursor); i < Math.max(newcur, cursor); i++){
            buffer.append((char) (byte) byteBuffer.get(i));
        }
        cursor = newcur;
        return buffer.toString();
    }
}