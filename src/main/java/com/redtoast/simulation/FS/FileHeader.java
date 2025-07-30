package com.redtoast.simulation.FS;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.base.LangError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Bytes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileHeader implements Exposable {
    public final Filepath internal;
    public final OpeningMode mode;
    public final FileSystem fs;
    public boolean canRead;
    public boolean canWrite;
    public boolean open = true;
    public int cursor = 0;
    public LinkedList<Byte> byteBuffer = new LinkedList<>();
    public FileHeader(Filepath filepath, FileSystem fs, OpeningMode mode){
        internal = filepath;
        this.mode = mode;
        this.fs = fs;
        canRead = mode.canRead();
        canWrite = mode.canWrite();
        try{
            if (filepath.exists() && !mode.truncate() && filepath.canRead()){
                if (mode.binary()){
                    for (byte _byte : filepath.readAllBinary()){
                        byteBuffer.add(_byte);
                    }
                }else{
                    for (byte _byte : filepath.readAll().getBytes()){
                        byteBuffer.add(_byte);
                    }
                }
            }
            if (mode.create()) filepath.createNewFile();
        }catch (Throwable e){
            if (e instanceof IOException){
                throw new LangError(e.getMessage());
            }else{
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Exposed
    public void flush(){
        if (!open) throw new LangError("Attempt to use a closed file");
        try{
            if (mode.canWrite()) {
                byte[] data = new byte[byteBuffer.size()];
                for (int i = 0; i < byteBuffer.size(); i++){
                    data[i] = byteBuffer.get(i);
                }
                if (mode.binary()){
                    internal.writeBinary(data);
                }else{
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
    public void write(Bytes bytes){
        if (!open) throw new LangError("Attempt to use a closed file");
        if (!canWrite) throw new LangError("Access denied");
        if (mode.binary()){
            for (byte _byte : bytes.getData()){
                byteBuffer.add(_byte);
            }
        }else{
            for (byte _byte : new String(bytes.getData(), StandardCharsets.UTF_8).getBytes()){
                byteBuffer.add(_byte);
            }
        }
    }

    @Exposed
    public Value read(){
        return read("l");
    }

    private int indexof(byte[] data, char charicter){
        for (int i = 0; i < data.length; i++){
            if (data[i] == (byte) charicter) return i;
        }
        return -1;
    }

    @Exposed
    public Value read(String format){
        if (!open) throw new LangError("Attempt to use a closed file");
        if (!canRead) throw new LangError("Access denied");
        if (format.length()!=1)  throw new LangError("Invalid format");
        if (byteBuffer.size()==cursor) return Value.of("");
        return switch (format.charAt(0)){
            case 'l', 'L' -> {
                List<Byte> bytes = byteBuffer.subList(cursor, byteBuffer.size());
                byte[] data = new byte[bytes.size()];
                for (int i =0; i < bytes.size(); i++){
                    data[i] = bytes.get(i);
                }
                if (mode.binary()){
                    int endpoint = indexof(data, '\n');
                    if (endpoint==-1){
                        cursor = byteBuffer.size();
                        yield Value.of(data);
                    }
                    cursor = cursor + endpoint + 1;
                    if (format.charAt(0)=='l'){
                        yield Value.of(data);
                    }else{
                        byte[] newdata = new byte[data.length+1];
                        for (int i = 0; i <= data.length; i++){
                            newdata[i] = i==data.length ? data[i] : (byte) '\n';
                        }
                        yield Value.of(newdata);
                    }
                }else{
                    String text = new String(data, StandardCharsets.UTF_8);
                    int endpoint = text.indexOf("\n");
                    if (endpoint==-1){
                        cursor = byteBuffer.size();
                        yield Value.of(text);
                    }
                    cursor = cursor + endpoint + 1;
                    if (format.charAt(0)=='l'){
                        yield Value.of(text.substring(0,endpoint));
                    }else{
                        yield Value.of(text.substring(0,endpoint) + '\n');
                    }
                }
            }
            case 'a' -> {
                List<Byte> bytes = byteBuffer.subList(cursor, byteBuffer.size());
                cursor = byteBuffer.size();
                byte[] data = new byte[bytes.size()];
                for (int i =0; i < bytes.size(); i++){
                    data[i] = bytes.get(i);
                }
                if (mode.binary()){
                    yield Value.of(data);
                }else{
                    yield Value.of(new String(data, StandardCharsets.UTF_8));
                }
            }
            default -> throw new LangError("Invalid format");
        };
    }

    @Exposed
    public Value read(int amount){
        if (!open) throw new LangError("Attempt to use a closed file");
        if (!canRead) throw new LangError("Access denied");
        int newcur = cursor+amount;
        if (newcur<0){
            newcur = 0;
        }
        if (newcur>byteBuffer.size()){
            newcur = byteBuffer.size();
        }
        if (newcur==cursor) return Value.of("");
        if (mode.binary()){
            byte[] data = new byte[Math.abs(newcur-cursor)];
            for (int i = Math.min(newcur, cursor); i < Math.max(newcur, cursor); i++){
                data[i - Math.min(newcur, cursor)] = byteBuffer.get(i);
            }
            cursor = newcur;
            return Value.of(data);
        }else{
            StringBuilder buffer = new StringBuilder();
            for (int i = Math.min(newcur, cursor); i < Math.max(newcur, cursor); i++){
                buffer.append((char) (byte) byteBuffer.get(i));
            }
            cursor = newcur;
            return Value.of(buffer.toString());
        }
    }
}