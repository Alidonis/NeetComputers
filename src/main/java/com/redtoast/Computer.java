package com.redtoast;

import com.redtoast.graphics.BianaryGraphicsArray;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.lua.FileHandler;
import com.redtoast.lua.LuaVM;
import com.redtoast.lua.IDFactory;
import com.redtoast.neet.NeetComputers;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.UUID;

public class Computer {
    private static Logger debug = LoggerFactory.getLogger("NeetComputers:debug-computerInst");
    private LuaVM VM;
    private int pointer = 0;
    private boolean loaded = false;
    private int ROM = -1;
    private boolean isOn = false;
    private AnyEntity parent;
    private UUID uuid;
    private FileHandler FS;
    private RGBGraphicsArray Graphics;

    public boolean IsOn(){return isOn;}
    public int getPointer(String rootName){
        FileHandler.rootDir root = FS.findRoot(rootName);
        if (root != null){
            return root.pointer;
        }
        return 0;
    }

    public BianaryGraphicsArray getBianaryGraphics() {
        return null;
    }

    public RGBGraphicsArray getGraphics() {
        return Graphics;
    }

    private static class AnyEntity{
        public int type;
        private Entity entity;
        private BlockEntity blockEntity;
        public AnyEntity(Entity parentEntity){
            type = 0;
            entity = parentEntity;
        }
        public AnyEntity(BlockEntity parentEntity){
            type = 1;
            blockEntity = parentEntity;
        }
        public void markDirty(){
            switch (type){
                case 0:
                    return;
                case 1:
                    blockEntity.markDirty();
            }
        }
    }

    public Computer(BlockEntity Parent){
        parent = new AnyEntity(Parent);
        uuid = UUID.randomUUID();
        Graphics = new RGBGraphicsArray(640,480);
    }

    public boolean isLoaded(){return loaded;}
    private void Load(){
        loaded = true;
        if (NeetComputers.worldPath!=null){
            FS = new FileHandler(pointer,ROM,"null");
        }
        if (isOn){
            Start();
        }
    }
    public void Load(NbtCompound nbt){
        if (!loaded){
            pointer = nbt.getInt("UserPointer");
            ROM = nbt.getInt("ROMPointer");
            if (nbt.contains("isOn")){
                isOn = nbt.getBoolean("isOn");
            }else{
                isOn = false;
            }
            Load();
        }else{
            if (nbt.contains("isOn")){
                if (nbt.contains("isOn")!=isOn){
                    if (nbt.getBoolean("isOn")){
                        Start();
                    }else{
                        Stop();
                    }
                }
            }
        }
    }
    public void Load(int UserPointer, int ROMPointer){
        if (!loaded){
            pointer = UserPointer;
            ROM = ROMPointer;
            isOn = false;
            Load();
        }
    }

    public void Load(MinecraftServer GameServer){
        if (!loaded){
            assert GameServer != null;
            IDFactory.getServerState(GameServer);
            IDFactory.PointerIteration++;
            pointer = IDFactory.PointerIteration;
            ROM = -1;
            isOn = false;
            Load();
        }
    }

    public NbtCompound writeNBT(NbtCompound nbt){
        nbt.putInt("UserPointer", pointer);
        nbt.putInt("ROMPointer",ROM);
        nbt.putBoolean("isOn",isOn);
        return nbt;
    }

    public void Start(){
        if (!isOn && loaded){
            VM = new LuaVM(this,FS,pointer,ROM);
            isOn = true;
            markDirty();
        }
    }

    public void staticStart(){
        if (isOn && VM==null){
            VM = new LuaVM(this,FS,pointer,ROM);
        }
    }

    public void Stop(){
        if (isOn && loaded){
            VM=null;
            isOn=false;
            markDirty();
        }
    }

    private void markDirty(){
        parent.markDirty();
    }

    private void step(){
        if (loaded){
            if (isOn && VM!=null){
                if (VM.isDead()){
                    Stop();
                }else{
                    if (isOn) {
                        VM.tick();
                    }
                }
            }
        }
    }

    public void Tick(){
        if (loaded){
            if (NeetComputers.worldPath!=null && FS==null){
                FS = new FileHandler(pointer,ROM,"null");
            }
            if (NeetComputers.worldPath!=null){
                if (isOn && VM==null) {
                    Start();
                }
                step();
            }
        }
    }
}
