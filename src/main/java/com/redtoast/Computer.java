package com.redtoast;

import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.GraphicsScreenHandler;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.lua.FileHandler;
import com.redtoast.lua.LuaVM;
import com.redtoast.lua.IDFactory;
import com.redtoast.neet.NeetComputers;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
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
    private BinaryGraphicsArray BinGraphics;
    private RGBGraphicsArray Graphics;

    public boolean IsOn(){return isOn;}
    public int getPointer(String rootName){
        FileHandler.rootDir root = FS.findRoot(rootName);
        if (root != null){
            return root.pointer;
        }
        return 0;
    }

    public BinaryGraphicsArray getBinaryGraphics() {
        return BinGraphics;
    }
    public void setBinaryGraphics(BinaryGraphicsArray graphics) {
        BinGraphics = graphics;
    }
    public void drawBinaryGraphics(){

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
                    if (blockEntity.getWorld() != null && !blockEntity.getWorld().isClient) {
                        blockEntity.getWorld().updateListeners(blockEntity.getPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), Block.NOTIFY_ALL);
                    }
            }
        }
    }

    public Computer(BlockEntity Parent){
        parent = new AnyEntity(Parent);
        uuid = UUID.randomUUID();
        Graphics = new RGBGraphicsArray(64,48);
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

    public void Tick(World world){
        if (loaded){
            if (NeetComputers.worldPath!=null && FS==null){
                FS = new FileHandler(pointer,ROM,"null");
            }
            if (NeetComputers.worldPath!=null){
                if (isOn && VM==null) {
                    Start();
                }
                step();
                for (PlayerEntity p : world.getPlayers()) {
                    if (p.currentScreenHandler instanceof GraphicsScreenHandler g && g.comp.computer == this) {
                        PacketByteBuf temp = PacketByteBufs.create();
                        Graphics.writeScreenToPacketBuf(temp);
                        ServerPlayNetworking.send((ServerPlayerEntity) p, NeetComputers.SCREEN_PACKET_ID, temp);
                    }
                }
            }
        }
    }
}
