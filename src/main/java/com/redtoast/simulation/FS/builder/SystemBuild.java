package com.redtoast.simulation.FS.builder;

import com.google.gson.*;
import com.redtoast.simulation.FS.BuildError;
import com.redtoast.simulation.FS.FileHelper;
import com.redtoast.simulation.FS.Partition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.LinkedList;

public class SystemBuild {
    public LinkedList<Partition> partitions = new LinkedList<>();
    public LinkedList<String> blacklist = new LinkedList<>();
    public String entrypoint;

    public SystemBuild(SystemPreset preset){
        switch (preset){
            case NEETOS -> {
                entrypoint = "bios:bios.lua";
                partitions.add(new Partition("bios",false,false,"-2"));
                partitions.add(new Partition("system",false,false,null));
                partitions.add(new Partition("user",false,false,null));
            }
            case CRAFTOS -> {
                entrypoint = "bios:bios.lua";
                partitions.add(new Partition("bios",false,true,"-6"));
                partitions.add(new Partition("rom",false,false,"-7"));
                partitions.add(new Partition("user",false,false,null));
            }
        }
    }

    public SystemBuild(String json, int pointer) throws BuildError, JsonSyntaxException{
        JsonElement element = JsonParser.parseString(json);
        if (!element.isJsonObject()){
            throw new BuildError("Json must be object");
        }
        JsonObject object = element.getAsJsonObject();
        if (!object.has("entrypoint")){
            throw new BuildError("Build config expected entrypoint, got null");
        }
        if (!object.get("entrypoint").isJsonPrimitive() || !object.getAsJsonPrimitive("entrypoint").isString()){
            throw new BuildError("Build config 'entrypoint' expected string");
        }
        entrypoint = object.get("entrypoint").getAsString();
        if (!object.has("partitions")){
            throw new BuildError("Build config expected partitions, got null");
        }
        if (!object.get("partitions").isJsonArray()){
            throw new BuildError("Build config 'partitions' must be array");
        }
        JsonArray partitionsArray = object.getAsJsonArray("partitions");
        for (JsonElement partition : partitionsArray.asList()){
            if (!partition.isJsonObject()){
                throw new BuildError("Build config expected partition as object, got '" + partition + '\'');
            }
            JsonObject partobject = partition.getAsJsonObject();
            if (!partobject.has("path")){
                throw new BuildError("Build config expected partition 'path', got null");
            }
            String path = partobject.getAsString();
            boolean readonly = false;
            boolean hidden = false;
            String source = null;

            if (partobject.has("readonly") && !object.get("readonly").isJsonPrimitive() || !partobject.getAsJsonPrimitive("readonly").isBoolean()){
                throw new BuildError("Build config expected 'readonly' boolean, got "+partobject.get("readonly"));
            }else if(partobject.has("readonly")){
                readonly = partobject.getAsJsonPrimitive("readonly").getAsBoolean();
            }

            if (partobject.has("hidden") && !object.get("hidden").isJsonPrimitive() || !partobject.getAsJsonPrimitive("hidden").isBoolean()){
                throw new BuildError("Build config expected 'hidden' boolean, got "+partobject.get("hidden"));
            }else if(partobject.has("hidden")){
                hidden = partobject.getAsJsonPrimitive("hidden").getAsBoolean();
            }

            if (partobject.has("source") && !object.get("source").isJsonPrimitive()){
                throw new BuildError("Build config expected 'source' String/hard-address, got "+partobject.get("source"));
            }else if(partobject.has("source")){
                if (partobject.getAsJsonPrimitive("source").isNumber()){
                    if (partobject.getAsJsonPrimitive("source").getAsInt()>=0){
                        throw new BuildError("Build config expected 'source' String/hard-address, got "+partobject.get("source"));
                    }
                }
                source = partobject.getAsJsonPrimitive("source").getAsString();
            }

            partitions.add(new Partition(path, readonly, hidden, source));
        }
        String path = FileHelper.normalize(entrypoint);
        BuildError badEntrypoint = new BuildError("Build config expected 'entrypoint' <partition path>:<address path>.<file extension>");
        if (!FileHelper.isAbsulute(path) || FileHelper.validatePathStatic(path)){
            throw badEntrypoint;
        }

        boolean check = false;
        String root = path.split(":")[1];
        for (Partition partition : partitions){
            if (partition.path().equals(root)) {
                check = true;
                break;
            }
        }
        if (!check) throw new BuildError("Build config 'entrypoint' doesn't refer to an assigned partition");
    }

    public SystemBuild(NbtCompound data){
        entrypoint = data.getString("entrypoint");
        NbtList partitions = data.getList("partitions", 10);
        for (NbtElement compound : partitions.toArray(new NbtElement[]{})){
            if (compound instanceof NbtCompound NBTpartition){
                Partition partition = new Partition(
                        NBTpartition.getString("path"),
                        NBTpartition.getBoolean("readonly"),
                        NBTpartition.getBoolean("hidden"),
                        String.valueOf(-NBTpartition.getInt("source"))
                );
                this.partitions.add(partition);
            }
        }
        if (data.contains("blacklist")) {
            NbtList blacklist = data.getList("blacklist", 8);
            for (NbtElement compound : blacklist.toArray(new NbtElement[]{})) {
                if (compound instanceof NbtString NbtPath) {
                    this.blacklist.add(NbtPath.asString());
                }
            }
        }
    }

    public NbtCompound save(){
        NbtCompound assembly = new NbtCompound();
        assembly.putString("entrypoint", entrypoint);
        NbtList partitions = new NbtList();
        for (Partition value : this.partitions) {
            NbtCompound NBTpartition = new NbtCompound();
            Partition partition = value;
            NBTpartition.putString("path", partition.path());
            NBTpartition.putBoolean("readonly", partition.readOnly());
            NBTpartition.putBoolean("hidden", partition.hidden());
            NBTpartition.putInt("source", FileHelper.isSourceHardAddress(partition.source()) ? -Integer.valueOf(partition.source()) : 0);
            partitions.add(NBTpartition);
        }
        assembly.put("partitions", partitions);
        if (!this.blacklist.isEmpty()){
            NbtList blacklist = new NbtList();
            for (String value : this.blacklist) {
                blacklist.add(NbtString.of(value));
            }
            assembly.put("blacklist", blacklist);
        }
        return assembly;
    }
}